package cc.datafabric.adapter.lib.exchange

import cc.datafabric.adapter.lib.sys.Logger
import com.mongodb.client.model.CreateViewOptions
import com.mongodb.client.model.IndexOptions
import com.mongodb.client.model.Indexes
import org.bson.Document

object ViewManager {
    //todo mongo не создает индекс повторно если он уже существует, но нужно доработать чтобы минимизировать количество обращений к БД




    private var enabled = false

    fun createViews(){
        if (!enabled) return
        ExchangeSettingsRepository.views.forEach {view->
            ExchangeDatabase.getCollection(view.output!!).drop()
            ExchangeDatabase.db.createView(view.output!!,view.input!!, view.pipeline!!)
        }
    }

    fun enable(){
        enabled = true
    }


}