> # java 集合框架



# 0.Preliminary

java数据接口框架中有两个大哥分别是`Collection`和`Map`

集合类继承了迭代器接口`Iterable<T>`,那么作为前置知识,我们必须要全面的了解该接口以及`Iterator<T>`类

```java
public interface Iterable<T> {
    Iterator<T> iterator(); //抽象方法,需要实现,返回一个迭代器用于遍历(这是集合特有的)
    
    
    default void forEach(Consumer<? super T> action) {  //默认方法,做foreach 其中使用了增强for,增强for底层实际就是迭代器
        Objects.requireNonNull(action);
        for (T t : this) {
            action.accept(t);
        }
    }

    default Spliterator<T> spliterator() {  //多线程分批遍历器
        return Spliterators.spliteratorUnknownSize(iterator(), 0);
    }
}
```



```java
public interface Iterator<E> {
    boolean hasNext(); //当前指向的元素是否有值
    E next(); //返回当前指针指向的元素,指针指向下一个
    default void remove() { 
        throw new UnsupportedOperationException("remove");
    }
    default void forEachRemaining(Consumer<? super E> action) {
        Objects.requireNonNull(action);
        while (hasNext())
            action.accept(next());
    }
}
```



![图片1](https://woldier-pic-repo-1309997478.cos.ap-chengdu.myqcloud.com/woldier/2023/08/1ec136274ed42a930b9db94ebb633c87.png)

对于Iterator对象,初始时,指针指向集合中的第一个元素.

`hasNext`会判断当前指针指向的位置是否有元素,如果有的话那么返回true,如果没有那么返回的是false.返回了false表明后面就没有元素了.

需要注意的是,一个实例化的迭代器对象只能使用一次. 因为遍历完之后, 指针是指向最尾部的地方, 但是Iterator对象并没有提供指针重新指向起始的元素.所以没办法用该对象再次遍历,只能重新产生一个.

`next`方法仅在调用过`hasNext`返回true后才能进行调用. `next`方法做的事情是返回当前指针指向的元素,然后指针移动,指向下一个.

> removes from the underlying collection the last element returned by this iterator (optional operation). This method can be called only once per call to next. The behavior of an iterator is unspecified if the underlying collection is modified while the iteration is in progress in any way other than by calling this method.

`remove`方法为默认方法,该方法仅在调用过`next`后才能进行调用(并且是可选项). 该方法的作用是将next返回的元素从集合中删除.

示例如下:

```java
List<String> list = new ArrayList<>();
//添加元素
list.add("王1");
list.add("李2");
list.add("张3");

Iterator<String> iter =  list.iterator();
while(iter.hasNext){
   
       String elem = iter.next;
    if( condition(elem) ){  //如果满足某种情况
      	iter.remove(); //删除iter.next返回的元素
       }
    
}

```

> Performs the given action for each remaining element until all elements have been processed or the action throws an exception. Actions are performed in the order of iteration, if that order is specified. Exceptions thrown by the action are relayed to the caller.

`forEachRemaining`方法为默认方法, 对后续还没有遍历的元素做相同的操作,操作通过`Consumer<? super E> action`参数传递操作.



# 1.Collection

写在前面 为什么Java中的Collection类都继承了抽象类还要实现抽象类的接口？

refer : https://blog.csdn.net/weixin_50276625/article/details/115060330

> The root interface in the collection hierarchy. A collection represents a group of objects, known as its elements. Some collections allow duplicate elements and others do not. Some are ordered and others unordered. The JDK does not provide any direct implementations of this interface: it provides implementations of more specific subinterfaces like Set and List. This interface is typically used to pass collections around and manipulate them where maximum generality is desired.
>
> `collection`是集合层级结构中最顶层的接口. 一个集合代表了一个种类的对象(或者称为元素)的汇总. 一些集合的实现允许冗余的元素(相同的元素),一些集合的实现其元素则是不重复的. 一些实现其元素排列时有序的, 而有的则是无序的. `JDK`不提供任何直接的`Collection`的实现类:而是提供了 更加具体的子接口如`Set`, `List`等. 该接口通常用于传递集合，并在需要最大通用性的情况下对其进行操作。
>
> or multisets (unordered collections that may contain duplicate elements) should implement this interface directly.
>
> `Bags(背包)`或多集合（可能包含重复元素的无序集合）应直接实现此接口
>
> All general-purpose Collection implementation classes (which typically implement Collection indirectly through one of its subinterfaces) should provide two "standard" constructors: a void (no arguments) constructor, which creates an empty collection, and a constructor with a single argument of type Collection, which creates a new collection with the same elements as its argument. In effect, the latter constructor allows the user to copy any collection, producing an equivalent collection of the desired implementation type. There is no way to enforce this convention (as interfaces cannot contain constructors) but all of the general-purpose Collection implementations in the Java platform libraries comply.
>
> 所有的通用集合实现类(通常实现`Collection`的子接口来简介实现Collection接口)应该提供两个'标准的'构造器: 一个空参(没有参数)的构造器,其创建一个空的集合;以及含有一个构造参数(类型是`Collection`)的构造器,该构造器创建一个与其构造参数对象元素类型相同的`Collection`对象. 实际上, 后者允许用户拷贝任意的集合类对象, 用于创建一个期望的集合实现类型且含有相同元素的集合对象. 没有办法强制执行这一约定（因为接口不能包含构造函数），但 Java 平台库中的所有通用集合实现都遵守了这一约定。
>
> The "destructive" methods contained in this interface, that is, the methods that modify the collection on which they operate, are specified to throw UnsupportedOperationException if this collection does not support the operation. If this is the case, these methods may, but are not required to, throw an UnsupportedOperationException if the invocation would have no effect on the collection. For example, invoking the addAll(Collection) method on an unmodifiable collection may, but is not required to, throw the exception if the collection to be added is empty.
>
> 该接口中包含的"破环性"的方法, 即该方法修改了其维护的集合元素. 如果实现类不支持此方法那么会抛出`UnsupportedOperationException `. 在这种情况下，如果调用对集合没有影响，这些方法可能会（但不是必须）抛出一个 `UnsupportedOperationException` 异常. 例如, 在一个不可修改的的集合上调用`addAll(Collection) `方法可能会（但不是必须）抛出一个 `UnsupportedOperationException` 异常.
>
> Some collection implementations have restrictions on the elements that they may contain. For example, some implementations prohibit null elements, and some have restrictions on the types of their elements. Attempting to add an ineligible element throws an unchecked exception, typically NullPointerException or ClassCastException. Attempting to query the presence of an ineligible element may throw an exception, or it may simply return false; some implementations will exhibit the former behavior and some will exhibit the latter. More generally, attempting an operation on an ineligible element whose completion would not result in the insertion of an ineligible element into the collection may throw an exception or it may succeed, at the option of the implementation. Such exceptions are marked as "optional" in the specification for this interface.
>
> 一些集合的实现对于其中包含的元素有着强烈的约束. 举个例子, 一些集合的实现拒绝`null`元素, 另一些则对它持有的元素的类型有着很高的要求.  尝试添加一个不受支持类型的元素将会抛出`unchecked exception`异常(典型的异常如`NullPointerException `,`ClassCastException`). 尝试查询一个存在与集合中的不受支持类型的元素可能会抛出异常, 或者简单的返回false; 一些实现类可能会表现出第一种特性(添加),另一些实现类则会表现出第二种(查询).  
>
> 通常, 尝试对一个不是支持类型的元素进行操作不会成功不会插入该不支持的元素到集合中并可能会抛出异常, 或者也许会成功,这取决于实现类. (更一般地说，对不符合条件的元素进行操作时，如果操作完成后没有将不符合条件的元素插入到集合中，则可能会抛出异常，也可能会成功，由实现者自行决定。这种异常在接口规范中被标记为 "可选"。)
>
> It is up to each collection to determine its own synchronization policy. In the absence of a stronger guarantee by the implementation, undefined behavior may result from the invocation of any method on a collection that is being mutated by another thread; this includes direct invocations, passing the collection to a method that might perform invocations, and using an existing iterator to examine the collection.
>
> 所有的集合实现需要自行决定自己的同步策略. 由于实现类较强确保性的缺乏, 未定义的行为(不安全的行为)源自于集合中任意方法被多个线程共享的访问. 这包括直接调用、将集合传递给可能执行调用的方法，以及使用现有迭代器检查集合。
>
> Many methods in Collections Framework interfaces are defined in terms of the equals method. For example, the specification for the contains(Object o) method says: "returns true if and only if this collection contains at least one element e such that (`o==null ? e==null : o.equals(e)`)." This specification should not be construed to imply that invoking Collection.contains with a non-null argument o will cause o.equals(e) to be invoked for any element e. Implementations are free to implement optimizations whereby the equals invocation is avoided, for example, by first comparing the hash codes of the two elements. (The Object.hashCode() specification guarantees that two objects with unequal hash codes cannot be equal.) More generally, implementations of the various Collections Framework interfaces are free to take advantage of the specified behavior of underlying Object methods wherever the implementor deems it appropriate.
>
> 集合框架接口中的许多方法都是通过等价方法定义的. 举个例子, `contains(Object o)`的描述说:"返回true如果这个集合中包含有至少一个元素且满足  (`o==null ? e==null : o.equals(e)`)".本规范不应被解释为 暗示实现者考虑调用 Collection.contains 时的非空参数 o 将导致调用任何元素 e 的 o.equals(e)。实现者可自由实现优化，从而避免调用 equals，例如，首先比较两个元素的散列代码。(Object.hashCode()规范保证哈希代码不相等的两个对象不可能相等）。更一般地说，各种集合框架接口的实现者可以自由利用底层对象方法的指定行为，只要实现者认为合适即可。
>
> Some collection operations which perform recursive traversal of the collection may fail with an exception for self-referential instances where the collection directly or indirectly contains itself. This includes the clone(), equals(), hashCode() and toString() methods. Implementations may optionally handle the self-referential scenario, however most current implementations do not do so.
> This interface is a member of the Java Collections Framework.
>
> 一些集合的递归遍历操作对于直接或间接包含自身引用的元素可能会抛出一个异常来标志失败.  .实现类可能选择性的处理自引用的情况,然而大多数当前的实现都没有这样做.
>
> 这个接口是java集合框架的



`Collection` 继承了 接口

```java
public interface Collection<E> extends Iterable<E> {
    //..................
```



```java
    int size();
    boolean isEmpty();
    boolean contains(Object o);
    Iterator<E> iterator(); //来自Iterable接口
    Object[] toArray();
    <T> T[] toArray(T[] a);
    boolean add(E e);
    boolean remove(Object o);
    boolean containsAll(Collection<?> c);
    boolean addAll(Collection<? extends E> c);
    boolean removeAll(Collection<?> c);
    boolean retainAll(Collection<?> c);
    void clear();
    boolean equals(Object o);
    int hashCode();


   
```



默认方法

```java
 default boolean removeIf(Predicate<? super E> filter) {
        Objects.requireNonNull(filter);
        boolean removed = false;
        final Iterator<E> each = iterator();
        while (each.hasNext()) {
            if (filter.test(each.next())) {
                each.remove();
                removed = true;
            }
        }
        return removed;
    }

	@Override
    default Spliterator<E> spliterator() {  //重写Iterable接口的默认方法
        return Spliterators.spliterator(this, 0);
    }


    default Stream<E> stream() {
        return StreamSupport.stream(spliterator(), false);
    }



    default Stream<E> parallelStream() {
        return StreamSupport.stream(spliterator(), true);
    }

```

`Collection`重写了`Iterable<E> `接口的默认方法`spliterator`,以及重新定义了`Iterator<E> iterator()`方法.

以及重新定义了`Object`类的`boolean equals(Object o)`,`int hashCode()`方法,等待子实现类实现.

继承了来自于`Iterable<E>`

![image-20230816182642345](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\image-20230816182642345.png)



## 1.1 AbstractCollection

> This class provides a skeletal implementation of the Collection interface, to minimize the effort required to implement this interface.
> To implement an unmodifiable collection, the programmer needs only to extend this class and provide implementations for the iterator and size methods. (The iterator returned by the iterator method must implement hasNext and next.)
>
> 本抽象类提供Collection接口实现的骨架, 来最小化实现Collection接口所需要的工作. 为了实现不可更改的集合, 编程人员只需要继承此抽象类并且提供迭代器`iterator `方法和`size`方法的实现.
>
> To implement a modifiable collection, the programmer must additionally override this class's add method (which otherwise throws an UnsupportedOperationException), and the iterator returned by the iterator method must additionally implement its remove method.
> The programmer should generally provide a void (no argument) and Collection constructor, as per the recommendation in the Collection interface specification.
>
> 为了实现一个可修改的集合, 编程者必须额外的重写本抽象类的`add`方法(此方法在抽象类中抛出`UnsupportedOperationException`异常), 并且`iterator  `方法返回的迭代器对象必须额外的实现迭代器的`remove`方法. 编程者一般需要根据Collection接口中提出的建议, 提供空参(void)和有一个接受集合类型参数的构造器.
>
> The documentation for each non-abstract method in this class describes its implementation in detail. Each of these methods may be overridden if the collection being implemented admits a more efficient implementation.
>
> 该类中每个非抽象方法的文档都详细描述了其实现方法。如果要实现的集合需要更有效的实现方法，则可以重载这些方法。

查看`AbstractCollection`抽象类的方法结构,其方法结构大部分来自于接口,自身拓展了两个方法.详细的接口见下图的描述.

![image-20230817164701446](https://woldier-pic-repo-1309997478.cos.ap-chengdu.myqcloud.com/woldier/2023/08/6237f1e2d5a7c25b8c1e4aa7307c428f.png)

对于两个抽象方法没什么好说的,子类实现即可.

```java
    public abstract Iterator<E> iterator();

    public abstract int size();
```



对于实现的`Collection`接口的方法除了`add(E e)`方法有点特殊之外,其他方法的实现基本上是基于迭代器(这里以`contains(Object o)`方法为例子)

```java
public boolean add(E e) {
     throw new UnsupportedOperationException();
}


public boolean contains(Object o) {
        Iterator<E> it = iterator();//创建一个迭代器
        if (o==null) { //如果传入的o为nulll
            while (it.hasNext())
                if (it.next()==null)
                    return true;
        } else { //如果参数不为null
            while (it.hasNext())
                if (o.equals(it.next()))
                    return true;
        }
        return false;
    }
```



重写了Object的toString方法(底层仍然使用的是迭代器)

```java
   public String toString() {
        Iterator<E> it = iterator();
        if (! it.hasNext()) //如果没有元素
            return "[]";

        StringBuilder sb = new StringBuilder(); //创建一个String构造器
        sb.append('['); //添加左括号
        for (;;) {
            E e = it.next();
            sb.append(e == this ? "(this Collection)" : e); // 当包含了自己时, 打印(this Collection)
            if (! it.hasNext())
                return sb.append(']').toString();
            sb.append(',').append(' ');
        }
    }
```



自定义的静态方法,和属性值

```java
    /**
     * The maximum size of array to allocate.
     * Some VMs reserve some header words in an array.
     * Attempts to allocate larger arrays may result in
     * OutOfMemoryError: Requested array size exceeds VM limit
     * 最大可以分配的数组大小. 一些VM保有一些header words在array中, 尝试分配更大一些的数组将会抛出异常.
     */
    private static final int MAX_ARRAY_SIZE = Integer.MAX_VALUE - 8;

    /**
     * Reallocates the array being used within toArray when the iterator
     * returned more elements than expected, and finishes filling it from
     * the iterator.
     * 当iterator返回了超过预期数量的元素,那么将重分配被toArray方法中使用的数组,并且完成从迭代器中返回数据填充新的数组.
     * @param r the array, replete with previously stored elements 数组，其中包含先前存储的元素 
     * @param it the in-progress iterator over this collection  之前遍历所使用的迭代器对象
     * @return array containing the elements in the given array, plus any  
     *         further elements returned by the iterator, trimmed to size
     *         数组，其中包含给定数组中的元素，以及迭代器返回的其他元素，并按大小修剪
     */
    @SuppressWarnings("unchecked")
    private static <T> T[] finishToArray(T[] r, Iterator<?> it) {
        int i = r.length; //得到之前数组的起始长度,或者说是指向尾部元素的指针
        while (it.hasNext()) { //当迭代器中还有元素时
            int cap = r.length; //复制一份数组当前的长度
            if (i == cap) {  //如果尾部元素指针与当期的数组长度一致
                int newCap = cap + (cap >> 1) + 1;  //cap进行扩展 cap = 1.5*cap +1
                // overflow-conscious code
                if (newCap - MAX_ARRAY_SIZE > 0)  //判断新的cap是否超过最大限制
                    newCap = hugeCapacity(cap + 1); //超过时的处理
                r = Arrays.copyOf(r, newCap);  //对System.arraycopy进行封装的方法,相当于返回了一个扩容后的数组
            }
            r[i++] = (T)it.next(); //做赋值
        }
        // trim if overallocated
        return (i == r.length) ? r : Arrays.copyOf(r, i); //按大小做裁剪
    }

    private static int hugeCapacity(int minCapacity) {
        if (minCapacity < 0) // overflow
            throw new OutOfMemoryError
                ("Required array size too large");
        return (minCapacity > MAX_ARRAY_SIZE) ?
            Integer.MAX_VALUE :
            MAX_ARRAY_SIZE;
    }
```



上面的`finishToArray`方法是对于调用`toArray`的时候进行的额外处理,那么我们需要去看看

```java
    public Object[] toArray() {
        // Estimate size of array; be prepared to see more or fewer elements
        Object[] r = new Object[size()]; //生成一个结果集数组
        Iterator<E> it = iterator(); //创建一个新的迭代器对象
        for (int i = 0; i < r.length; i++) {
            //能够进入for循环那么说明当前指针还没有指到之前保存的预测尾部
            if (! it.hasNext()) // fewer elements than expected 说明当前集合中的元素个数减少了
                return Arrays.copyOf(r, i); //按大小做裁剪
            r[i] = it.next(); //做赋值
        }
        return it.hasNext() ? finishToArray(r, it) : r;  //判断是否发生了元素的添加,发生了添加it.hasNext()返回true进入finishToArray方法,否则直接返回.
    }
```



## 1.2 List

### 1.2.1 Preliminary ListIterator

> An iterator for lists that allows the programmer to traverse the list in either direction, modify the list during iteration, and obtain the iterator's current position in the list. A ListIterator has no current element; its cursor position always lies between the element that would be returned by a call to previous() and the element that would be returned by a call to next(). An iterator for a list of length n has n+1 possible cursor positions, as illustrated by the carets (^) below:
>                                  Element(0)   Element(1)   Element(2)   ... Element(n-1)
>   cursor positions:  ^                   ^                   ^                    ^                          ^
>
> Note that the remove and set(Object) methods are not defined in terms of the cursor position; they are defined to operate on the last element returned by a call to next or previous().
> This interface is a member of the Java Collections Framework.

![image-20230817213450849](https://woldier-pic-repo-1309997478.cos.ap-chengdu.myqcloud.com/woldier/2023%2F08%2F90849acadd56930bbca570b243c52168.png)

### 1.2.2 List



> An ordered collection (also known as a sequence). The user of this interface has precise control over where in the list each element is inserted. The user can access elements by their integer index (position in the list), and search for elements in the list.
> Unlike sets, lists typically allow duplicate elements. More formally, lists typically allow pairs of elements e1 and e2 such that e1.equals(e2), and they typically allow multiple null elements if they allow null elements at all. It is not inconceivable that someone might wish to implement a list that prohibits duplicates, by throwing runtime exceptions when the user attempts to insert them, but we expect this usage to be rare.
>
> 一个有序的集合(也被称为顺序集合). 该接口的用户可以精确控制每个元素在列表中的插入位置. 用户可以根据整数索引(list中的位置)访问元素, 并且搜索list中的元素.   与set不同, list允许重复的元素. 更正式地说，列表通常允许成对的元素 e1 和 e2，即 e1.equals(e2)，如果允许空元素，则通常允许多个空元素.  不难想象, 有人可能希望一个list的实现杜绝重复情况, 当尝试插入重复元素抛出运行时异常,但是我们这样的使用情况是稀有的.
>
> The List interface places additional stipulations, beyond those specified in the Collection interface, on the contracts of the iterator, add, remove, equals, and hashCode methods. Declarations for other inherited methods are also included here for convenience.
> The List interface provides four methods for positional (indexed) access to list elements. Lists (like Java arrays) are zero based. Note that these operations may execute in time proportional to the index value for some implementations (the LinkedList class, for example). Thus, iterating over the elements in a list is typically preferable to indexing through it if the caller does not know the implementation.
>
> 除了集合接口中的规定外，列表接口还对iterator, add, remove, equals, and hashCode方法的合约做出了额外的规定。为了方便,此接口中重新声明了其他继承的方法. List接口提供了四个索引访问list元素的方法. 需要注意的是这些方法可能执行非常耗时的来访问list中的元素(比如说LinkedList). 因此,如果调用者不知道是哪一种实现,相比于使用位置索引,在list中通过迭代器进行遍历更加的好.
>
> The List interface provides a special iterator, called a ListIterator, that allows element insertion and replacement, and bidirectional access in addition to the normal operations that the Iterator interface provides. A method is provided to obtain a list iterator that starts at a specified position in the list.
>
> List接口提供了一个特殊的迭代器, 命名为`ListIterator`, 相较于`Iterator `提供的常规的操作该迭代器运行元素插入以及替换, 双向移动(next与prev).
>
> The List interface provides two methods to search for a specified object. From a performance standpoint, these methods should be used with caution. In many implementations they will perform costly linear searches.
>
> List接口提供了两个方法来查找特殊的对象. 从性能的立场看, 这些方法需要谨慎的使用. 在多数的实现中,这些方法表现为耗时的线性查找. 
>
> The List interface provides two methods to efficiently insert and remove multiple elements at an arbitrary point in the list.
> Note: While it is permissible for lists to contain themselves as elements, extreme caution is advised: the equals and hashCode methods are no longer well defined on such a list.
>
> List接口提供了两个方法用于高效的在list中的任意位置的插入和删除元素. 注意: 当list包含自己为元素成为可能, 强烈的建议equals 和 hashCode 方法已不再定义明确. 
>
> Some list implementations have restrictions on the elements that they may contain. For example, some implementations prohibit null elements, and some have restrictions on the types of their elements. Attempting to add an ineligible element throws an unchecked exception, typically NullPointerException or ClassCastException. Attempting to query the presence of an ineligible element may throw an exception, or it may simply return false; some implementations will exhibit the former behavior and some will exhibit the latter. More generally, attempting an operation on an ineligible element whose completion would not result in the insertion of an ineligible element into the list may throw an exception or it may succeed, at the option of the implementation. Such exceptions are marked as "optional" in the specification for this interface.
>
> 一些list的实现对于它们所包含的元素有着强烈的约束. 举个例子, 一些实现禁止null元素, 一些实现则对元素的type有较高的要求. 尝试插入一个不受支持的元素抛出`unchecked exception`通常为`NullPointerException`或者`ClassCastException`. 尝试查询一个不受支持的元素可能会抛出异常,或者简单的返回false. 一些实现展现第一种特性,一些则展现出另一种. 更一般地说，对不符合条件的元素进行操作时，如果操作完成后没有将不符合条件的元素插入到列表中，则可能会抛出异常，也可能会成功，由实现者自行决定。这种异常在本接口的规范中被标记为 "可选"。

### 



`List`继承了`Collection`接口

```java
public interface List<E> extends Collection<E> {
    //..........................
}
```

List接口的方法结构

![image-20230817174831966](https://woldier-pic-repo-1309997478.cos.ap-chengdu.myqcloud.com/woldier/2023/08/dfebfa1bb08f086a335714cca9e6421c.png)

```java

    int size();
    boolean isEmpty();
    boolean contains(Object o);
    Iterator<E> iterator();
    Object[] toArray();
    <T> T[] toArray(T[] a);
    boolean add(E e);
    boolean remove(Object o);
    boolean containsAll(Collection<?> c);
    boolean addAll(Collection<? extends E> c);
    boolean removeAll(Collection<?> c);
    boolean retainAll(Collection<?> c);
    void clear();
    boolean equals(Object o);
    int hashCode();
```

重写了`spliterator`方法(该方法是重写的Collection,而Collection重写的Interable)

```java
    @Override
    default Spliterator<E> spliterator() {
        return Spliterators.spliterator(this, Spliterator.ORDERED);
    }
```



继承了Collection的五个方法(见上图),以及Iterable的一个方法`foreach`



最后就是自定义了一些抽象方法,以及给出了两个default方法

```java
    default void replaceAll(UnaryOperator<E> operator) {
        Objects.requireNonNull(operator);
        final ListIterator<E> li = this.listIterator();
        while (li.hasNext()) {
            li.set(operator.apply(li.next()));
        }
    }
    default void sort(Comparator<? super E> c) {
        Object[] a = this.toArray();
        Arrays.sort(a, (Comparator) c);
        ListIterator<E> i = this.listIterator();
        for (Object e : a) {
            i.next();  //指向下一个
            i.set((E) e); //设置值
        }
    }
```



剩下的方法则为定义的抽象方法

```java

    boolean addAll(int index, Collection<? extends E> c);
  
    E get(int index);
    
    E set(int index, E element);

    void add(int index, E element);
 
    E remove(int index);

    int indexOf(Object o);

    int lastIndexOf(Object o);

    ListIterator<E> listIterator();
    
    ListIterator<E> listIterator(int index);

   
    List<E> subList(int fromIndex, int toIndex);


```

