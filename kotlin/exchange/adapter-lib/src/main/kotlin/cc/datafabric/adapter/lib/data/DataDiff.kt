package cc.datafabric.adapter.lib.data


import cc.datafabric.adapter.lib.sys.Logger


class DataDiff() {



//    private var profileMap: RdfMap = RdfMap()
//    fun getProfileMap(): RdfMap {
//       return profileMap
//    }
//    fun setProfileMap(value: RdfMap) {
//        profileMap = value
//    }

    private var forDiffModel: IDataModel? = null
    private var revDiffModel: IDataModel?= null
    constructor(forwardDiffModel: IDataModel? = null, reverseDiffModel: IDataModel? =null) : this() {
        forDiffModel = forwardDiffModel
        revDiffModel = reverseDiffModel
//        forwardDiffModel?.diff=this
//        reverseDiffModel?.diff=this
    }

    fun isEmpty():Boolean{
       return  Logger.traceFun {
            val value = forDiffModel==null && revDiffModel==null
            Logger.traceData(value.toString())
            return@traceFun value
        }
    }

    fun getForwardDiffModel(): IDataModel? {
        return  forDiffModel
    }

    fun getReverseDiffModel(): IDataModel? {
        return  revDiffModel
    }





}