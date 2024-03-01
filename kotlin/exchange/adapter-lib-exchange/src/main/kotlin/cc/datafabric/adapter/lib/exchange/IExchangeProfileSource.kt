package cc.datafabric.adapter.lib.exchange

interface IExchangeProfileSource {
    fun getProperties(): Map<String, ExchangeProfile.PropertyInfo>
    fun getInverseMap(): Map<String, String>
    fun getClassNamespaces(): Map<String, String>
    fun getPropertyNamespaces(): Map<String, String>
}