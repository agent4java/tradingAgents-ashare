package com.example.tradingagents.data.tushare;

import com.example.tradingagents.data.api.FundamentalsService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@ConditionalOnProperty(name = "tradingagents.data.vendor", havingValue = "tushare")
public class TushareFundamentalsService implements FundamentalsService {

    private static final DateTimeFormatter TUSHARE_DATE = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final TushareClient client;

    public TushareFundamentalsService(TushareClient client) {
        this.client = client;
    }

    @Override
    public Map<String, Object> getFundamentals(String symbol, LocalDate tradeDate) {
        Map<String, String> params = new HashMap<>();
        params.put("ts_code", symbol);
        String fields = "ts_code,ann_date,end_date,eps,bps,roe,roe_waa,roe_dt,roa,npt_ratio,grossprofit_margin," +
                "netprofit_margin,profits_to_gr,saleinfo_to_or,rd_exp_to_gr,debt_to_assets,assets_to_eqt";
        List<Map<String, Object>> rows = client.request("fina_indicator", params, fields);
        if (rows.isEmpty()) {
            return Collections.singletonMap("note", "无基本面指标数据");
        }
        return toReadableFundamentals(rows.get(0));
    }

    @Override
    public List<Map<String, Object>> getBalanceSheet(String symbol, LocalDate tradeDate, int limit) {
        Map<String, String> params = new HashMap<>();
        params.put("ts_code", symbol);
        String fields = "ts_code,ann_date,end_date,total_assets,total_cur_assets,total_liab,total_cur_liab,total_hldr_eqy_exc_min_int";
        List<Map<String, Object>> rows = client.request("balancesheet", params, fields);
        return rows.stream().limit(limit).map(this::toReadableMap).collect(Collectors.toList());
    }

    @Override
    public List<Map<String, Object>> getCashflow(String symbol, LocalDate tradeDate, int limit) {
        Map<String, String> params = new HashMap<>();
        params.put("ts_code", symbol);
        String fields = "ts_code,ann_date,end_date,n_cashflow_act,n_cashflow_inv_act,n_cash_flows_fnc_act";
        List<Map<String, Object>> rows = client.request("cashflow", params, fields);
        return rows.stream().limit(limit).map(this::toReadableMap).collect(Collectors.toList());
    }

    @Override
    public List<Map<String, Object>> getIncomeStatement(String symbol, LocalDate tradeDate, int limit) {
        Map<String, String> params = new HashMap<>();
        params.put("ts_code", symbol);
        String fields = "ts_code,ann_date,end_date,revenue,operate_profit,total_profit,n_income";
        List<Map<String, Object>> rows = client.request("income", params, fields);
        return rows.stream().limit(limit).map(this::toReadableMap).collect(Collectors.toList());
    }

    private Map<String, Object> toReadableFundamentals(Map<String, Object> r) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("代码", r.get("ts_code"));
        m.put("报告期", r.get("end_date"));
        m.put("每股收益", r.get("eps"));
        m.put("每股净资产", r.get("bps"));
        m.put("ROE", r.get("roe"));
        m.put("ROA", r.get("roa"));
        m.put("净利润率", r.get("netprofit_margin"));
        m.put("资产负债率", r.get("debt_to_assets"));
        return m;
    }

    private Map<String, Object> toReadableMap(Map<String, Object> r) {
        Map<String, Object> m = new LinkedHashMap<>(r);
        return m;
    }
}
