class VariablesMap<T : Any> : AbstractMutableMap<Variable, T>() {
    override var size: Int = 0
        private set
    private var _a: Array<T?>? = null
    private var _h: HashMap<Variable, T>? = null

    private fun a(index: Int): Array<T?> {
        val a = _a
        if (a != null && a.size > index) return a
        val newSize = maxOf(minCapacity, index + 1)
        @Suppress("UNCHECKED_CAST")
        return (if (a == null) arrayOfNulls<Any?>(newSize) as Array<T?> else a.copyOf(newSize)).also {
            _a = it
        }
    }

    private fun h(): HashMap<Variable, T> =
        _h ?: HashMap<Variable, T>().also { _h = it }

    override fun isEmpty(): Boolean = size == 0

    override fun put(key: Variable, value: T): T? {
        val i = key.variableIndex
        if (i < 0) {
            val h = h()
            return h.put(key, value).also {
                if (it == null) size++
            }
        }
        val a = a(i)
        return a[i].also {
            a[i] = value
            if (it == null) size++
        }
    }

    override fun get(key: Variable): T? {
        val i = key.variableIndex
        if (i < 0) return _h?.get(key)
        return _a?.getOrNull(i)
    }

    override fun containsKey(key: Variable): Boolean = get(key) != null

    override fun clear() {
        size = 0
        _a?.fill(null)
        _h?.clear()
    }

    private var _entries: Entries<T>? = null
    override val entries: MutableSet<MutableMap.MutableEntry<Variable, T>> =
        _entries ?: Entries<T>(this).also { _entries = it }

    private class Entries<T : Any>(val map: VariablesMap<T>) : AbstractMutableSet<MutableMap.MutableEntry<Variable, T>>() {
        override val size: Int
            get() = map.size
        override fun isEmpty(): Boolean = map.isEmpty()

        override fun add(entry: MutableMap.MutableEntry<Variable, T>): Boolean =
            map.put(entry.key, entry.value) != entry.value

        override fun clear() = map.clear()

        private inner class IndexedEntry(val i: Int) : MutableMap.MutableEntry<Variable, T> {
            override fun setValue(newValue: T): T {
                val a = map.a(i)
                return a[i]!!.also { a[i] = newValue }
            }

            override val key: Variable
                get() = makeVariable(i)
            override val value: T
                get() = map._a!![i]!!
        }

        override fun iterator(): MutableIterator<MutableMap.MutableEntry<Variable, T>> =
            object : AbstractIterator<MutableMap.MutableEntry<Variable, T>>(), MutableIterator<MutableMap.MutableEntry<Variable, T>> {
                private var i = -1
                private var iter: MutableIterator<MutableMap.MutableEntry<Variable, T>>? = null

                override fun computeNext() {
                    val a = map._a
                    if (a != null && i < a.size) {
                        while (true) {
                            i++
                            if (i >= a.size) break
                            if (a[i] != null) {
                                setNext(IndexedEntry(i))
                                return
                            }
                        }
                    }
                    val h = map._h
                    if (h == null) {
                        done()
                        return
                    }
                    val iter = iter ?: h.iterator().also { iter = it }
                    if (iter.hasNext()) {
                        setNext(iter.next())
                        return
                    }
                    done()
                }

                override fun remove() {
                    TODO("Not yet implemented")
                }
        }
    }
}

private val minCapacity = 4