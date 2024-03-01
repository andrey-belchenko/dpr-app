package cc.datafabric.adapter.lib.data

import cc.datafabric.adapter.lib.rdf.RdfFactory

object DataFactory {
    // Заморочки с интерфейсами IData... и Factory, чтобы исключить прямые зависимости от реализации на jena,
    // но пока это единственная реализация
    fun createModel(): IDataModel {
        return RdfFactory.createModel()
    }
}