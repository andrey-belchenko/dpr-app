package cc.datafabric.adapter.lib.exchange



object ExchangeAgent {
    fun start() {
        IndexManager.reset()
        ExchangeListener.listen {
            ExchangeProcessor.action(it.changedCollections)
        }
    }
}