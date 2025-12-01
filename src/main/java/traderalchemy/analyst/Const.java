package traderalchemy.analyst;

public interface Const {
    
    public enum SearchTool {
        brave,
        perplexity,
        openrouter;
    }
    
    public enum FlashNewsSource {
        investing,
        yfinance,
        chaincatcher,
        coindesk,
        cointelegraph,
        cnbc,
        marketwatch,
        wallstreetcn,
        others;
    }

    public enum FlashNewsSite {
        chaincatcher,
        finnhub,
        wallstreetcn,
        investing,
        yfinance;
    }
    
    public enum ArticleSource {
        investing,
        yfinance,
        chaincatcher,
        coindesk,
        glassnode;
    }
    
    public enum ArticleSite {
        chaincatcher,
        glassnode;
    }
    
    public enum InsightCategory {
        research,
        flashnews,
        article,
        user;
    }

    public enum Exchange {
        binance,
        bybit,
        coinbase,
        okx,
        bitget,
        kraken,
        mexc,
        bitfinex,
        hyperliquid,
        pionex,
        gateio,
        kucoin,
        xt;
    }
    
    public enum StrategyStatus {
        activated,
        activating,
        deactivated,
        pending_deactivate,
        deactivating;
    }

    public enum SearchDirection {
        analyze,
        compare,
        contrast,
        define,
        describe,
        discuss,
        enumerate,
        evaluate,
        how,
        identify,
        illustrate,
        summarize,
        trace,
        why;
    }
}
