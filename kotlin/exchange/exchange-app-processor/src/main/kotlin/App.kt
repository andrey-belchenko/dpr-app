package cc.datafabric.exchange.app.proc


import cc.datafabric.adapter.lib.exchange.*


object App {
    fun main() {
   
//        ExchangePipelineRepository.get("СозданиеУчасткаМагистрали")
        IndexManager.enable()
        ViewManager.enable()
        ExchangeAgent.start()
//        val profileText = PlatformClientProfiles.getProfile()
//
//        val model =RdfFactory.modelFromQuads(profileText)
//        val v = profileText

    }





}