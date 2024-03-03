package cc.datafabric.exchange.cim.model


import kotlin.reflect.KProperty


class Links<T: ModelObject> (private val owner: ModelObject, private val inverseProperty: KProperty<*>?) : MutableSet<T> {
    private val set: MutableSet<T> = mutableSetOf()

    override val size: Int
        get() = set.size

    override fun contains(element: T): Boolean = set.contains(element)

    override fun containsAll(elements: Collection<T>): Boolean = set.containsAll(elements)

    override fun isEmpty(): Boolean = set.isEmpty()

    override fun iterator(): MutableIterator<T> = set.iterator()

    override fun add(element: T): Boolean = addInverse(setOf(element),set.add(element))

    override fun addAll(elements: Collection<T>): Boolean = addInverse(elements,set.addAll(elements))

    override fun clear() {
        val elements = set.toSet()
        set.clear()
        removeInverse(elements,elements.any())
        owner.changed()
    }

    override fun remove(element: T): Boolean = removeInverse(setOf(element),set.remove(element))

    override fun removeAll(elements: Collection<T>): Boolean = removeInverse(elements,set.removeAll(elements.toSet()))

    override fun retainAll(elements: Collection<T>): Boolean {
        val elementsToRemove = set.subtract(elements.toSet())
        return removeInverse(elementsToRemove, set.retainAll(elements.toSet()))
    }

    private fun removeInverse(elements: Collection<T>,hasChanges: Boolean):Boolean{
        if (hasChanges) {
            elements.forEach { element ->
                if (inverseProperty != null) {
                    ReflectionUtils.removeLink(element, inverseProperty, owner)
                }
            }
            owner.changed()
        }
        return hasChanges
    }

    private fun addInverse(elements: Collection<T>,hasChanges: Boolean):Boolean{
        if (hasChanges){
            elements.forEach { element->
                if (inverseProperty!=null){
                    ReflectionUtils.addLink(element, inverseProperty, owner)
                }
            }
            owner.changed()
        }
        return hasChanges
    }
}