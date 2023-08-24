> # java 集合框架



# 0. Preliminary

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



# 1. Collection

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



### 1.2.3 AbstractList

> This class provides a skeletal implementation of the List interface to minimize the effort required to implement this interface backed by a "random access" data store (such as an array). For sequential access data (such as a linked list), AbstractSequentialList should be used in preference to this class.
>
> 本抽象类提供了实现List接口的骨架, 最小化了支持随机存取数据存储(如数组)的情况下实现List接口需要做的努力.  对于顺序访问的数据(比如说链表), 应该优先使用  `AbstractSequentialList `而非此类. 
>
> To implement an unmodifiable list, the programmer needs only to extend this class and provide implementations for the get(int) and size() methods.
>
> 为了实现一个不可修改的list, 编程者只需要继承此类,并且提供`get(int)` 和`size() `方法的实现即可.
>
> To implement a modifiable list, the programmer must additionally override the set(int, E) method (which otherwise throws an UnsupportedOperationException). If the list is variable-size the programmer must additionally override the add(int, E) and remove(int) methods.
>
> 为了实现一个可修改的数组, 编程者需要二外的重写`set(int, E)`方法(否则的话该方法抛出 `UnsupportedOperationException` 异常). 如果该list是一个可变大小的数组, 那么需要额外的实现`add(int, E)`和`remove(int)`方法
>
> The programmer should generally provide a void (no argument) and collection constructor, as per the recommendation in the Collection interface specification.
>
> 编程者根据Collection中提出的建议, 通常需要提供一个空参的构造方法以及一个可以接口Collection类型参数的构造方法, 
>
> Unlike the other abstract collection implementations, the programmer does not have to provide an iterator implementation; the iterator and list iterator are implemented by this class, on top of the "random access" methods: get(int), set(int, E), add(int, E) and remove(int).
>
> 不像其它的集合的实现, 编程者无需提供迭代器的实现;基于"随机访问"的方法 `get(int), set(int, E), add(int, E) and remove(int)`   `iterator `和`list iterator`已经被本类实现, 
>
> The documentation for each non-abstract method in this class describes its implementation in detail. Each of these methods may be overridden if the collection being implemented admits a more efficient implementation.
>
> 

AbstractList的结构相较于前面的类更加的复杂,因此本小结准备一点一点的介绍

先给个总体的图

![image-20230818105818230](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\image-20230818105818230.png)



#### 1.2.3.1 Itr类

>   # AbstractList的内部类是迭代器的实现类(与前面匿名内部类的方式不同)

> 这里有一个小细节是这个类是私有的且没有加static
>
> 这样new出来的Itr对象是直接与创建的`AbstractList`实现类的对象关联的. 可以访问实现类对象中的方法.
>
> 这也是生成的Itr对象能够遍历对应的list的原因.(注:ReentrantLock中的ConditionObject对象也是这样工作的)
>
> 关于更详细的解释以及使用可以参考博客:
>
> https://blog.csdn.net/jianghuafeng0/article/details/109194468
>
> https://zhuanlan.zhihu.com/p/61735448

![image-20230818111925614](https://woldier-pic-repo-1309997478.cos.ap-chengdu.myqcloud.com/woldier/2023/08/1dd7d97a47b17bfbe9b036fce71203a2.png)

```java
private class Itr implements Iterator<E> {
        /**
         * Index of element to be returned by subsequent call to next.
         * 后续调用next方法将会返回的元素的索引值
         */
        int cursor = 0;  //指针

        /**
         * Index of element returned by most recent call to next or
         * previous.  Reset to -1 if this element is deleted by a call
         * to remove.
         * 最近调用next而被返回的元素的索引值
         */
        int lastRet = -1;   

        /**
         * The modCount value that the iterator believes that the backing
         * List should have.  If this expectation is violated, the iterator
         * has detected concurrent modification.
         * 迭代器认为与其绑定的List应该具有的modCount值. 如果期望值是`violated`那么迭代器可以检测到并发修改
         */
        int expectedModCount = modCount;

        public boolean hasNext() {  //hasNext方法
            return cursor != size(); //比较当前的游标是否大于list的size 参见0.Preliminary中的图
        }

        public E next() {
            checkForComodification();  //检查是否被并发修改
            try {
                int i = cursor;  //暂存游标值
                E next = get(i);  //访问对应元素
                lastRet = i;  // 最近返回指针指向当前游标
                cursor = i + 1;  //游标加1
                return next; //返回暂存的元素
            } catch (IndexOutOfBoundsException e) { //检测按照索引get元素出错
                checkForComodification(); //检查是否是被修正了
                throw new NoSuchElementException();  //
            }
        }

        public void remove() {
            if (lastRet < 0)  //如果最后访问的索引为-1则 抛出异常(还有就是控制每次一调用next后只能调用remove成功一次)
                throw new IllegalStateException();
            checkForComodification();  //检查并发修改

            try {
                AbstractList.this.remove(lastRet); //删除,modified会变化
                if (lastRet < cursor) //如果lastRet < cursor说明是调用的next,lastRet = cursor否则表明调用的是previous
                    cursor--; 
                lastRet = -1; //设置lastRet为-1防止再次调用
                expectedModCount = modCount;  //更新保存的预期modCount
            } catch (IndexOutOfBoundsException e) {
                throw new ConcurrentModificationException();
            }
        }

        final void checkForComodification() {
            if (modCount != expectedModCount)  //检查值是否相等
                throw new ConcurrentModificationException();  //抛出并发修改异常
        }
    }
```

#### 1.2.3.2 ListItr

![image-20230818134533107](https://woldier-pic-repo-1309997478.cos.ap-chengdu.myqcloud.com/woldier/2023/08/d54cd4f64db5243199990277bbffc600.png)

```java
 private class ListItr extends Itr implements ListIterator<E> {
        ListItr(int index) {  //构造函数,传入当前的游标值
            cursor = index;
        }

        public boolean hasPrevious() {
            return cursor != 0; //判断当前游标是否为0,为0则没有previous了
        }

        public E previous() {
            checkForComodification();  //检查修改
            try {
                int i = cursor - 1; //游标减一,并且拷贝一份到i中
                E previous = get(i); //获取i索引对应的元素
                lastRet = cursor = i; //最近返回的对象指针lastRet指向i
                return previous;
            } catch (IndexOutOfBoundsException e) { //抛出异常
                checkForComodification(); //检查是否是因为修改造成的
                throw new NoSuchElementException(); 
            }
        }

        public int nextIndex() { //返回next指针
            return cursor;
        }

        public int previousIndex() { //返回prev指针
            return cursor-1;
        }

        public void set(E e) { //修改元素
            if (lastRet < 0) //如果lastRet = -1 那么说明刚做过set,或者是remove操作,那么不允许再调用set
                throw new IllegalStateException();
            checkForComodification(); 

            try {
                AbstractList.this.set(lastRet, e);//设置对应索引的新值
                expectedModCount = modCount;//重写得到期望modCount
            } catch (IndexOutOfBoundsException ex) {
                throw new ConcurrentModificationException();
            }
        }

        public void add(E e) { //
            checkForComodification();

            try {
                int i = cursor;
                AbstractList.this.add(i, e);
                lastRet = -1;
                cursor = i + 1;
                expectedModCount = modCount;
            } catch (IndexOutOfBoundsException ex) {
                throw new ConcurrentModificationException();
            }
        }
    }
```

#### 1.2.3.3 实现的List的方法

> 其中的 add,clear,iterator 方法也是重写的父类AbstractCollection中的方法

![image-20230818154207977](https://woldier-pic-repo-1309997478.cos.ap-chengdu.myqcloud.com/woldier/2023/08/162215b0474f65a7051fbd3e6372b917.png)

```java
	/**
     * Appends the specified element to the end of this list (optional
     * operation). 添加一个元素到list的末尾
     *
     * <p>Lists that support this operation may place limitations on what
     * elements may be added to this list.  In particular, some
     * lists will refuse to add null elements, and others will impose
     * restrictions on the type of elements that may be added.  List
     * classes should clearly specify in their documentation any restrictions
     * on what elements may be added.
     * 支持本操作的list对插入的元素会有限制. 特别的, 一些list会拒绝添加null元素, 一些则对添加元素的类型做了要      * 求. List实现类需要清晰明白的注明插入元素的限制.
     * <p>This implementation calls {@code add(size(), e)}. 
     * 本方法调用 add(size(), e) 方法
     * <p>Note that this implementation throws an 
     * {@code UnsupportedOperationException} unless
     * {@link #add(int, Object) add(int, E)} is overridden.
     * 需要注意本方法会抛出 {@code UnsupportedOperationException} 除非{@link #add(int, Object) add(int, E)} 方法被重写
     * @param e element to be appended to this list 即将插入到list中元素
     * @return {@code true} (as specified by {@link Collection#add}) 返回值的含义参考Collection的说明
     * @throws UnsupportedOperationException if the {@code add} operation
     *         is not supported by this list 如果add方法不被实现类支持
     * @throws ClassCastException if the class of the specified element
     *         prevents it from being added to this list  指定的元素类型不支持被插入到此list中
     * @throws NullPointerException if the specified element is null and this
     *         list does not permit null elements 插入的元素为null,并且list不支持null元素
     * @throws IllegalArgumentException if some property of this element
     *         prevents it from being added to this list 如果该元素的某些属性阻止它被添加到该列表中 
     */
    public boolean add(E e) {
        add(size(), e);
        return true;
    }


    /**
     * Inserts the specified element at the specified position in this list (optional operation). 
     * Shifts the element currently at that position (if any) and any subsequent elements to the 
     * right (adds one to their indices).
     * 取代特定位置处的元素
     * <p>This implementation always throws an 本实现总是抛出UnsupportedOperationException
     * {@code UnsupportedOperationException}.
     *
     * @throws UnsupportedOperationException  如果set操作不被类支持
     * @throws ClassCastException            {@inheritDoc} 当前元素不支持插入到list中
     * @throws NullPointerException          {@inheritDoc} 不支持null元素
     * @throws IllegalArgumentException      {@inheritDoc} 元素的某些属性使得其不被支持插入到list中
     * @throws IndexOutOfBoundsException     {@inheritDoc} 索引越界
     */
    public E set(int index, E element) {
        throw new UnsupportedOperationException();
    }

    /**
	 *在list长得特定位置插入元素. 滑动当前位置的元素以及右侧(索引值更大)的元素(如果有的话).
     */
    public void add(int index, E element) {
        throw new UnsupportedOperationException();
    }

    /**
	 *删除特定位置插入元素. 滑动右侧(索引值更大)的元素(如果有的话).
     */
 	public E remove(int index) {
        throw new UnsupportedOperationException();
    }
    /**
	 * 返回list中第一个出现的特定值的元素, 或者如果是不存在则返回-1.
	 * 更进一步说返回满足条件(o==null ? get(i)==null : o.equals(get(i)))的最小的索引值
        * 如果不存在则返回-1
        */
public int indexOf(Object o) {
        ListIterator<E> it = listIterator();  //得到一个ListIterator
        if (o==null) {
        while (it.hasNext())
        if (it.next()==null)
        return it.previousIndex();
        } else {
        while (it.hasNext())
        if (o.equals(it.next()))
        return it.previousIndex();
        }
        return -1;
        }

    /**
     * 返回list中第最后出现的特定值的元素, 或者如果是不存在则返回-1.
     * 更进一步说返回满足条件(o==null ? get(i)==null : o.equals(get(i)))的最大的索引值
     * 如果不存在则返回-1
     */
    public int lastIndexOf(Object o) {
        ListIterator<E> it = listIterator(size());  //初始化一个游标指向末尾的迭代器
        if (o==null) { //允许空元素的遍历
            while (it.hasPrevious()) //如果还有前驱
                if (it.previous()==null) //得到前驱,判断是非为null
                    return it.nextIndex(); //if成立,找到元素
        } else { //对于非空元素的遍历
            while (it.hasPrevious())
                if (o.equals(it.previous())) //判断相等
                    return it.nextIndex();
        }
        return -1;
    }

    public void clear() {
        removeRange(0, size());
    }
	
   /**
     * 将所有集合中的元素添加到list中,滑动当前位置的元素以及右侧(索引值更大)的元素(如果有的话).
     * 新元素出现的顺序与Collection返回的迭代器遍历得到元素的顺序是一致的.
     * 如果指定的集合在操作过程中被修改，该操作的行为将是未定义的。(请注意，如果指定的集合是此列表且非空，则会出现这种情况）。
     */
    public boolean addAll(int index, Collection<? extends E> c) {
        rangeCheckForAdd(index); //检擦index的合法性
        boolean modified = false;  // 修改tag
        for (E e : c) {  //遍历c中的元素 (增强for,底层任然是遍历器)
            add(index++, e);   
            modified = true;
        }
        return modified;
    }


   public Iterator<E> iterator() { //返回一个迭代器对象
        return new Itr();
    }

    public ListIterator<E> listIterator() { //返回一个list迭代器对象,默认游标为0
        return listIterator(0);
    }

    public ListIterator<E> listIterator(final int index) { //返回一个list迭代器对象, 指定
        rangeCheckForAdd(index);

        return new ListItr(index);
    }


   public List<E> subList(int fromIndex, int toIndex) { //返回子list 
        return (this instanceof RandomAccess ?
                new RandomAccessSubList<>(this, fromIndex, toIndex) :
                new SubList<>(this, fromIndex, toIndex));
    }
```



#### 1.2.3.4 重定义的List中的抽象方法

![image-20230818171053964](https://woldier-pic-repo-1309997478.cos.ap-chengdu.myqcloud.com/woldier/2023/08/6eeb6f225d034be83efc2a8e5184487a.png)

```java
    /**
     * Returns the element at the specified position in this list.
     *
     * @throws IndexOutOfBoundsException if the index is out of range (index < 0 || index >= size())
     */
    abstract public E get(int index);
```

#### 1.2.3.5 实现List,Collection中的equals和hashCode方法(同时也重写了AbstractCollection中的方法)

```java

    /**
     * Compares the specified object with this list for equality.  Returns
     * {@code true} if and only if the specified object is also a list, both
     * lists have the same size, and all corresponding pairs of elements in
     * the two lists are <i>equal</i>.  (Two elements {@code e1} and
     * {@code e2} are <i>equal</i> if {@code (e1==null ? e2==null :
     * e1.equals(e2))}.)  In other words, two lists are defined to be
     * equal if they contain the same elements in the same order.<p>
     * 比较一个特定的对象与本list的等价性. 当且仅当待比较的object是list的时候,与本list有相同的size,
     * 并且对应的元素页相等时才返回true. 
     * 换句话说, 两个list被定义为相等,当且仅当他们包含相同的元素,且顺序相同
     * 
     * This implementation first checks if the specified object is this
     * list. If so, it returns {@code true}; if not, it checks if the
     * specified object is a list. If not, it returns {@code false}; if so,
     * it iterates over both lists, comparing corresponding pairs of elements.
     * If any comparison returns {@code false}, this method returns
     * {@code false}.  If either iterator runs out of elements before the
     * other it returns {@code false} (as the lists are of unequal length);
     * otherwise it returns {@code true} when the iterations complete.
     * 本实现首先检查待比较的对象是不是本list, 如果是那么返回true,如果不是那么检查该对象的类型是不是list
     * ,如果不是则返回false. 如果是, 那么遍历本list与目标list, 比较相应的迭代元素. 如果任意一次比较返回了
     * false, 那么方法返回fasle. 如果任意一个迭代器提前结束了(说明长度不等)任然返回false. 否则说明两个list
     * 的元素是相同的, 返回true. 
     * @param o the object to be compared for equality with this list
     * @return {@code true} if the specified object is equal to this list
     */
    public boolean equals(Object o) {
        if (o == this) //判断代比较对象是否指向的是自己
            return true;
        if (!(o instanceof List)) //判断是否是list
            return false;

        ListIterator<E> e1 = listIterator(); //获取本对象的迭代器
        ListIterator<?> e2 = ((List<?>) o).listIterator(); //获取待比较对象的迭代器
        while (e1.hasNext() && e2.hasNext()) {  //循环遍历
            E o1 = e1.next();
            Object o2 = e2.next();
            if (!(o1==null ? o2==null : o1.equals(o2)))
                return false;
        }
        return !(e1.hasNext() || e2.hasNext());
    }

    /**
     * Returns the hash code value for this list.
     * 
     * <p>This implementation uses exactly the code that is used to define the
     * list hash function in the documentation for the {@link List#hashCode}
     * method.
     *
     * @return the hash code value for this list
     */
    public int hashCode() {
        int hashCode = 1;
        for (E e : this)
            hashCode = 31*hashCode + (e==null ? 0 : e.hashCode());
        return hashCode;
    }
```

```java
hashCode = 31*hashCode + (e==null ? 0 : e.hashCode());
//其实也等同于
hashCode = ( hashCode<<5 )-hashCode + (e==null ? 0 : e.hashCode());
```



#### 1.2.3.6 继承的AbstractCollection的方法

![image-20230821105044616](https://woldier-pic-repo-1309997478.cos.ap-chengdu.myqcloud.com/woldier/2023/08/ef6f9979af86270a22bcdd98784fd4fa.png)

#### 1.2.3.7 继承的Collection的方法

![image-20230821110114597](https://woldier-pic-repo-1309997478.cos.ap-chengdu.myqcloud.com/woldier/2023/08/20319dbacfaa5e4f81acfb3ac24d7220.png)

#### 1.2.3.8 继承的 List的方法

![image-20230821111005539](https://woldier-pic-repo-1309997478.cos.ap-chengdu.myqcloud.com/woldier/2023/08/6a925d7cdfddea51bab3fc8a148e0bdb.png)

#### 1.2.3.9 自己拓展的方法

![image-20230821113340012](https://woldier-pic-repo-1309997478.cos.ap-chengdu.myqcloud.com/woldier/2023/08/ac8cc11e8175f963d4e7dbac5033d564.png)



```java
     /**
     * Removes from this list all of the elements whose index is between
     * {@code fromIndex}, inclusive, and {@code toIndex}, exclusive.
     * Shifts any succeeding elements to the left (reduces their index).
     * This call shortens the list by {@code (toIndex - fromIndex)} elements.
     * (If {@code toIndex==fromIndex}, this operation has no effect.)
     * 删除list中索引下标在区间[fromIndex,toIndex)左闭右开的元素. 移动所有右侧的后续节点.
     * 本方法调用使得list的长度消减,特别注意当toIndex==fromIndex时,不会对list产生任何的影响
     * <p>This method is called by the {@code clear} operation on this list
     * and its subLists.  Overriding this method to take advantage of
     * the internals of the list implementation can <i>substantially</i>
     * improve the performance of the {@code clear} operation on this list
     * and its subLists.
     * 本方法被list及其子list的clear方法调用. 可以基于实现的类型(如数组,或者链表)
     * 重写此方法将会提供更好的性能. 
     * <p>This implementation gets a list iterator positioned before
     * {@code fromIndex}, and repeatedly calls {@code ListIterator.next}
     * followed by {@code ListIterator.remove} until the entire range has
     * been removed.  <b>Note: if {@code ListIterator.remove} requires linear
     * time, this implementation requires quadratic time.</b>
     * 本方法的算法流程时先初始化一个ListIterator(设置游标位置为fromIndex).
     * 随后重复的调用ListIterator.next和ListIterator.remove直到待删除
     * @param fromIndex index of first element to be removed
     * @param toIndex index after last element to be removed
     */
    protected void removeRange(int fromIndex, int toIndex) {
        ListIterator<E> it = listIterator(fromIndex);
        for (int i=0, n=toIndex-fromIndex; i<n; i++) {
            it.next();
            it.remove();
        }
    }



    private void rangeCheckForAdd(int index) {
        if (index < 0 || index > size()) //判断size是否合法
            throw new IndexOutOfBoundsException(outOfBoundsMsg(index));
    }

	 private String outOfBoundsMsg(int index) { //组装打印消息的方法
        return "Index: "+index+", Size: "+size();
    }

```



#### 1.2.3.10 外部类SubList

```java
class SubList<E> extends AbstractList<E> {
    private final AbstractList<E> l;
    private final int offset;
    private int size;
    //.....................
    
```

该类使用了装饰器模式,它可以感知到l的变化,对本类操作相当于对list操作.

可以看一下它listIterator的源码

它拿到了成员变量`l`的迭代器,然后对迭代过程做了一些限制

```java
    public ListIterator<E> listIterator(final int index) {
        checkForComodification();//检查是否出现过modify
        rangeCheckForAdd(index);//检查索引的合法性

        return new ListIterator<E>() {
            private final ListIterator<E> i = l.listIterator(index+offset); //拿到成员变量l的迭代器

            public boolean hasNext() {  //检查是否还有下一个元素
                return nextIndex() < size;
            }

            public E next() {
                if (hasNext())
                    return i.next();
                else
                    throw new NoSuchElementException();
            }

            public boolean hasPrevious() {
                return previousIndex() >= 0;
            }

            public E previous() {
                if (hasPrevious())
                    return i.previous();
                else
                    throw new NoSuchElementException();
            }

            public int nextIndex() {
                return i.nextIndex() - offset;
            }

            public int previousIndex() {
                return i.previousIndex() - offset;
            }

            public void remove() {
                i.remove();
                SubList.this.modCount = l.modCount;
                size--;
            }

            public void set(E e) {
                i.set(e);
            }

            public void add(E e) {
                i.add(e);
                SubList.this.modCount = l.modCount;
                size++;
            }
        };
    }
```



#### 1.2.3.11 外部类RandomAccessSubList 

与`SubList`唯一的区别是继承了RandomAccess,其他没有任何区别

```java
class RandomAccessSubList<E> extends SubList<E> implements RandomAccess {
    RandomAccessSubList(AbstractList<E> list, int fromIndex, int toIndex) {
        super(list, fromIndex, toIndex);
    }

    public List<E> subList(int fromIndex, int toIndex) {
        return new RandomAccessSubList<>(this, fromIndex, toIndex);
    }
}

```



### 1.2.4 ArrayList

> Resizable-array implementation of the List interface. Implements all optional list operations, and permits all elements, including null. In addition to implementing the List interface, this class provides methods to manipulate the size of the array that is used internally to store the list. (This class is roughly equivalent to Vector, except that it is unsynchronized.)
>
> 可变大小的List接口实现. 实现了所有可选的list操作, 并且允许所有元素(包括null). 除了实现了List接口, 本类提供了维护内部用于list数组大小的方法. (本类基本上与`Vector.class`相同, 除了本类是线程不安全的)
>
> The size, isEmpty, get, set, iterator, and listIterator operations run in constant time. The add operation runs in amortized constant time, that is, adding n elements requires O(n) time. All of the other operations run in linear time (roughly speaking). The constant factor is low compared to that for the LinkedList implementation.
>
> `size`,`isEmpty`, `get`, `set`, `iterator,` and `listIterator`方法运行的时间是常量时间. `add`操作以摊销后的恒定时间运行，即添加 n 个元素需要 O(n) 时间. 所有的其他操作运行时间是线性的(大体上讲). 常量因子相较于LinkedList的实现是非常小的.
>
> Each ArrayList instance has a capacity. The capacity is the size of the array used to store the elements in the list. It is always at least as large as the list size. As elements are added to an ArrayList, its capacity grows automatically. The details of the growth policy are not specified beyond the fact that adding an element has constant amortized time cost.
>
> 每个ArrayList实例都有一个容量(capacity). 容量指的是List中用于存储元素的数组的长度. 其总是比list的当前大小大. 当元素被添加到一个ArrayList. 数组的容量发生动态的增长. 除了增加一个元素的摊销时间成本不变之外，对动态扩容的策略实现细节没有明确规定。
>
> An application can increase the capacity of an ArrayList instance before adding a large number of elements using the ensureCapacity operation. This may reduce the amount of incremental reallocation.
>
> 一个应用可以在添加一个大规模的元素之前使用`ensureCapacity `操作对ArrayList的容量进行扩容. 这或许可以减少重复调用扩容方法的次数.
>
> Note that this implementation is not synchronized. If multiple threads access an ArrayList instance concurrently, and at least one of the threads modifies the list structurally, it must be synchronized externally. (A structural modification is any operation that adds or deletes one or more elements, or explicitly resizes the backing array; merely setting the value of an element is not a structural modification.) This is typically accomplished by synchronizing on some object that naturally encapsulates the list. If no such object exists, the list should be "wrapped" using the Collections.synchronizedList method. This is best done at creation time, to prevent accidental unsynchronized access to the list:
>
> ​    List list = Collections.synchronizedList(new ArrayList(...));
>
> 需要注意的是本实现是线程不安全的. 如果ArrayList 实例被多个线程并发的访问, 并且至少有一个线程在对list的结构做修改, 那么则必须在外部保证`synchronized ` (结构修改指的是任何添加或者删除一个或者多个元素的操作, 或者更详细的说变更内部的数组; 稀有的设置元素的值不是结构的修改). 这通常是通过某个对象对象封装本list完成了. 如果没有这样的对象的存在话, 数组需要通过`Collections.synchronizedList` 方法来进行"包裹". 
>
> This is best done at creation time, to prevent accidental unsynchronized access to the list:
>
> ​    List list = Collections.synchronizedList(new ArrayList(...));
>
> The iterators returned by this class's iterator and listIterator methods are fail-fast: if the list is structurally modified at any time after the iterator is created, in any way except through the iterator's own remove or add methods, the iterator will throw a ConcurrentModificationException. Thus, in the face of concurrent modification, the iterator fails quickly and cleanly, rather than risking arbitrary, non-deterministic behavior at an undetermined time in the future.
>
> 本类 iterator ` 和`listIterator`方法返回的迭代器是有快速失败机制二点: 如果说迭代器被创建后, list的结构在任何时刻被被修正了(除了是在迭代器内部调用了remove或者add方法),那么迭代器将会抛出`ConcurrentModificationException`. 因此, 面临并发修改的时候, 迭代器干净利落的失败,而不是冒着在未来某个不确定的时间发生任意, 非确定行为的风险.  
>
> Note that the fail-fast behavior of an iterator cannot be guaranteed as it is, generally speaking, impossible to make any hard guarantees in the presence of unsynchronized concurrent modification. Fail-fast iterators throw ConcurrentModificationException on a best-effort basis. Therefore, it would be wrong to write a program that depended on this exception for its correctness: the fail-fast behavior of iterators should be used only to detect bugs.
>
> 总的来说, 需要注意迭代器的`fail-fast`行为不能确保无故障行为. 因为一般来说, 在非同步并发修改的情况下, 不可能做任何的硬性保护. `fail-fast`代器会尽力抛出 ConcurrentModificationException 异常。因此，如果程序的正确性依赖于该异常，那将是错误的：迭代器的`fail-fast`行为只能用于检测错误。



```java
public class ArrayList<E> extends AbstractList<E>
        implements List<E>, RandomAccess, Cloneable, java.io.Serializable
{
    //............................................
```



#### 1.2.4.1 Itr类

ArrayList内部定义了一个Iter类(需要使用此内部类,那么必定要重写父类的`iterator`和`listIterator`方法)

![image-20230821202540590](https://woldier-pic-repo-1309997478.cos.ap-chengdu.myqcloud.com/woldier/2023%2F08%2Fb05b003dbf1051b55a88a599f5c04b50.png)

由于ArrayList实现的遍历器需要访问内部维护的`elementData`变量,因此这里先介绍了下ArrayList里面有的变量

```java
	private static final long serialVersionUID = 8683452581122892189L; //序列化id

    /**
     * Default initial capacity.
     */
    private static final int DEFAULT_CAPACITY = 10; //默认的容量

    /**
     * Shared empty array instance used for empty instances.
     */
    private static final Object[] EMPTY_ELEMENTDATA = {}; //空的elemdata,所有空的ArrayList公用一个

    /**
     * Shared empty array instance used for default sized empty instances. We
     * distinguish this from EMPTY_ELEMENTDATA to know how much to inflate when
     * first element is added.
     */
    private static final Object[] DEFAULTCAPACITY_EMPTY_ELEMENTDATA = {}; //也是空的数组,不过该数组是使用默认size分配的,这样做是为了分辨初始size

    /**
     * The array buffer into which the elements of the ArrayList are stored.
     * The capacity of the ArrayList is the length of this array buffer. Any
     * empty ArrayList with elementData == DEFAULTCAPACITY_EMPTY_ELEMENTDATA
     * will be expanded to DEFAULT_CAPACITY when the first element is added.
     */
    transient Object[] elementData; // non-private to simplify nested class access

    /**
     * The size of the ArrayList (the number of elements it contains).
     *
     * @serial
     */
    private int size;
```



接下来看一下Itr源码, 其实大体上与AbstractList源码差不多的,唯一的差别是AbstractList用的是get(i)获取对应位置的元素,而ArrayList中是直接拿到elementData的引用. 

除此之外,还重写了`forEachRemaining`方法.

```java
private class Itr implements Iterator<E> {
        int cursor;       // index of next element to return
        int lastRet = -1; // index of last element returned; -1 if no such
        int expectedModCount = modCount;  

        Itr() {}

        public boolean hasNext() {
            return cursor != size;  //比较游标是否等于size判断越界
        }

        @SuppressWarnings("unchecked")
        public E next() {
            checkForComodification();
            int i = cursor;  // 保存当前游标
            if (i >= size)  // 如果i比size打 那么说明超出
                throw new NoSuchElementException();
            Object[] elementData = ArrayList.this.elementData;  //保存ArrayList中数组的引用
            if (i >= elementData.length)  //判断异常
                throw new ConcurrentModificationException();
            cursor = i + 1; //游标加一
            return (E) elementData[lastRet = i]; // 返回对应位置元素
        }

        public void remove() {
            if (lastRet < 0)  //如果lastRet小于零那么说明可能是刚刚初始化,都没调用过next;或者说已经调用了remove了,那么不允许重复调用
                throw new IllegalStateException();
            checkForComodification(); //检查并发修改

            try {
                ArrayList.this.remove(lastRet);  //删除元素
                cursor = lastRet;  //游标移动
                lastRet = -1; //重设lastRet
                expectedModCount = modCount; //重新设置expectedModCount
            } catch (IndexOutOfBoundsException ex) {
                throw new ConcurrentModificationException();
            }
        }

        @Override
        @SuppressWarnings("unchecked")
        public void forEachRemaining(Consumer<? super E> consumer) {
            Objects.requireNonNull(consumer);
            final int size = ArrayList.this.size;
            int i = cursor; //保存当前游标
            if (i >= size) {
                return;
            }
            final Object[] elementData = ArrayList.this.elementData;
            if (i >= elementData.length) {
                throw new ConcurrentModificationException();
            }
            while (i != size && modCount == expectedModCount) {
                consumer.accept((E) elementData[i++]);
            }
            // update once at end of iteration to reduce heap write traffic
            cursor = i;
            lastRet = i - 1;
            checkForComodification();
        }

        final void checkForComodification() {
            if (modCount != expectedModCount) //比较与ArrayList的modCount值是否一致
                throw new ConcurrentModificationException();
        }
    }
```

#### 1.2.4.2 ListItr

![image-20230822093550880](https://woldier-pic-repo-1309997478.cos.ap-chengdu.myqcloud.com/woldier/2023/08/d9532118661c6d58f5b2fc379c9997a8.png)

```java
    /**
     * An optimized version of AbstractList.ListItr
     */
    private class ListItr extends Itr implements ListIterator<E> {
        ListItr(int index) { // 构造参数设置初始游标
            super();
            cursor = index;
        }

        public boolean hasPrevious() { //是否还有前驱
            return cursor != 0; //判断游标是否指向0
        }

        public int nextIndex() {
            return cursor;
        }

        public int previousIndex() { //返回前驱指针
            return cursor - 1; 
        }

        @SuppressWarnings("unchecked")
        public E previous() { //游标左移
            checkForComodification(); //检查并发修改
            int i = cursor - 1; //保存游标减一的值
            if (i < 0) //判断是否小于零
                throw new NoSuchElementException();
            Object[] elementData = ArrayList.this.elementData; //得到ArrayList中的
            if (i >= elementData.length)
                throw new ConcurrentModificationException();
            cursor = i; //游标左移
            return (E) elementData[lastRet = i];//返回游标对应的元素
        }

        public void set(E e) {
            if (lastRet 这是为了只做一次modify < 0) //判断lastRet 这是为了只做一次modify
                throw new IllegalStateException();
            checkForComodification();

            try {
                ArrayList.this.set(lastRet, e);  // 设置元素
            } catch (IndexOutOfBoundsException ex) {
                throw new ConcurrentModificationException();
            }
        }

        public void add(E e) {
            checkForComodification(); //检查并发修改

            try {
                int i = cursor; //记录当前游标
                ArrayList.this.add(i, e); //添加元素
                cursor = i + 1; //游标加1
                lastRet = -1; //设置最后返回的元素游标为-1,防止调用remove,previous方法
                expectedModCount = modCount; //更新modify计数
            } catch (IndexOutOfBoundsException ex) {
                throw new ConcurrentModificationException();
            }
        }
    }
```

####  1.2.4.3 ArrayListSpliterator内部类

##### 1.2.4.3.1 preliminary

ArrayListSpliterator实现了Spliterator接口,因此首先要搞清楚Spliterator在干什么.

> An object for traversing and partitioning elements of a source. The source of elements covered by a Spliterator could be, for example, an array, a Collection, an IO channel, or a generator function.
>
> 一个用于遍历以及对源数据分块的对象. 被Spliterator 支持的数据源元素可以是来自数组,`Collection`,`IO channel`,和`generator function`
>
> A Spliterator may traverse elements individually (tryAdvance()) or sequentially in bulk (forEachRemaining()).
>
> 一个`Spliterator `可以单独(`tryAdvance()`) 或者分批顺序的 (`forEachRemaining()`).遍历元素
>
> A Spliterator may also partition off some of its elements (using trySplit) as another Spliterator, to be used in possibly-parallel operations. Operations using a Spliterator that cannot split, or does so in a highly imbalanced or inefficient manner, are unlikely to benefit from parallelism. Traversal and splitting exhaust elements; each Spliterator is useful for only a single bulk computation.
>
> 一个`Spliterator`可以将其部分元素分块成另一个`Spliterator`来用于可能的并行化操作. 使用不能分块,或者不能高度平衡分块`Spliterator ` 进行操作将不太可能从并行中获得性能的受益. 每个`Spliterator `只能用于一次批量计算.
>
> A Spliterator also reports a set of characteristics() of its structure, source, and elements from among ORDERED, DISTINCT, SORTED, SIZED, NONNULL, IMMUTABLE, CONCURRENT, and SUBSIZED. These may be employed by Spliterator clients to control, specialize or simplify computation. For example, a Spliterator for a Collection would report SIZED, a Spliterator for a Set would report DISTINCT, and a Spliterator for a SortedSet would also report SORTED. Characteristics are reported as a simple unioned bit set. Some characteristics additionally constrain method behavior; for example if ORDERED, traversal methods must conform to their documented ordering. New characteristics may be defined in the future, so implementors should not assign meanings to unlisted values.
>
> 一个`Spliterator `通过调用`characteristics()`方法可以获取到元素的结构等信息, 包括 ORDERED, DISTINCT, SORTED,  SIZED, NONNULL, IMMUTABLE, CONCURRENT, and SUBSIZED. 这些属性被`Spliterator `的client所选择用于特定的场景. 举个例子, `Collection`的`Spliterator`将会展现SIZED, `Set`则会展现出DISTINCT, `SortedSet `展现的则是SORTED属性. 属性简单的使用独特的bit位标识. 一些属性特征额外的约束方法的行为; 举个例子 如果是ORDERED,  遍历的方法必须确定其定义的顺序. 新的特征属性获取会在将来被定义, 因此实现类不应该赋予未定义的值的含义. 
>
> A Spliterator that does not report IMMUTABLE or CONCURRENT is expected to have a documented policy concerning: when
>
>  the spliterator binds to the element source; and detection of structural interference of the element source detected after binding. A late-binding Spliterator binds to the source of elements at the point of first traversal, first split, or first query for estimated size, rather than at the time the Spliterator is created. A Spliterator that is not late-binding binds to the source of elements at the point of construction or first invocation of any method. Modifications made to the source prior to binding are reflected when the Spliterator is traversed. After binding a Spliterator should, on a best-effort basis, throw ConcurrentModificationException if structural interference is detected. Spliterators that do this are called fail-fast. The bulk traversal method (forEachRemaining()) of a Spliterator may optimize traversal and check for structural interference after all elements have been traversed, rather than checking per-element and failing immediately.  
>
> 特征属性不是IMMUTABLE 或者CONCURRENT 的`Splterator`应该有着记录在文档中的策略,包括: 什么时候`Spliterator`与源元素绑定; 以及在绑定后元素结构干扰的检测. 一个后续绑定的`Splterator`在首次遍历,首次分片,首次查询size的时候才绑定元素源而不是当`Spliterator `创建的时候. 一个不是后续绑定的`Splterator`在调用构造方法或者是第一次调用任意一个方法时绑定元素源. 在绑定元素源之前做的修改会在`Splterator`遍历时反映出来. 基于最大努力交付基础, 在绑定了一个`Splterator`后,如果检测到结构性的修改, 那么会抛出 `ConcurrentModificationException ` 异常. 能够提供该功能的`Spliterators `称为`fail-fast`. `Spliterator `的批遍历方法(`forEachRemaining()`)乐观的遍历, 然后在遍历完成之后检测结构性的修改, 而不是检查每一个元素并且立即失败. 
>
> Spliterators can provide an estimate of the number of remaining elements via the estimateSize method. Ideally, as reflected in characteristic SIZED, this value corresponds exactly to the number of elements that would be encountered in a successful traversal. However, even when not exactly known, an estimated value value may still be useful to operations being performed on the source, such as helping to determine whether it is preferable to split further or traverse the remaining elements sequentially.
>
> `Spliterators `可以提供一个对剩余元素的数量的估计通过`estimateSize `方法. 理想情况下(作为SIZED特征的反映), 该值与成功遍历所扫描到的元素数量一致. 然而, 即使是不能确信的知道该值,一个估计的值仍然是对操作有益的, 比如说帮助判断是否需要进一步的细分还是遍历顺序的遍历剩余的元素. 
>
> Despite their obvious utility in parallel algorithms, spliterators are not expected to be thread-safe; instead, implementations of parallel algorithms using spliterators should ensure that the spliterator is only used by one thread at a time. This is generally easy to attain via serial thread-confinement, which often is a natural consequence of typical parallel algorithms that work by recursive decomposition. A thread calling trySplit() may hand over the returned Spliterator to another thread, which in turn may traverse or further split that Spliterator. The behaviour of splitting and traversal is undefined if two or more threads operate concurrently on the same spliterator. If the original thread hands a spliterator off to another thread for processing, it is best if that handoff occurs before any elements are consumed with tryAdvance(), as certain guarantees (such as the accuracy of estimateSize() for SIZED spliterators) are only valid before traversal has begun.
>
> 尽管本类的效用是在于并行化计算, `spliterators `并不是如期待的那样是线程安全的. 除此之外, 使用了`spliterators `并行算法的实现需要确保某个`spliterators `在一个特定的时刻是被单线程使用的. 这通常很容易通过串行线程配置来实现，而串行线程配置通常是通过递归分解工作的典型并行算法的自然结果。一个线程调用`trySplit()`或许提交返回的`Spliterator `给另一个线程, 另一个线程反过来会遍历或者进一步的分块`Spliterator `. 分块和遍历的具体行为是未定义的 如果说存在两个及以上的线程并发的操作同一个`Spliterator`. 如果原始线程提交一个`spliterator `给另一个线程处理, 最好是在使用 tryAdvance() 消耗任何元素之前进行移交，因为某些check（例如 SIZED 分割器的 estimateSize() 的准确性）只有在遍历开始之前才有效。
>
> Primitive subtype specializations of Spliterator are provided for int, long, and double values. The subtype default implementations of tryAdvance(Consumer) and forEachRemaining(Consumer) box primitive values to instances of their corresponding wrapper class. Such boxing may undermine any performance advantages gained by using the primitive specializations. To avoid boxing, the corresponding primitive-based methods should be used. For example, Spliterator.OfInt.tryAdvance(IntConsumer) and Spliterator.OfInt.forEachRemaining(IntConsumer) should be used in preference to Spliterator.OfInt.tryAdvance(Consumer) and Spliterator.OfInt.forEachRemaining(Consumer). Traversal of primitive values using boxing-based methods tryAdvance() and forEachRemaining() does not affect the order in which the values, transformed to boxed values, are encountered.
>
> Spliterator 的原始子类型特化适用于 int、long 和 double 值。tryAdvance(Consumer) 和 forEachRemaining(Consumer) 的子类型默认实现将初值框选为相应封装类的实例。



![image-20230824140928559](https://woldier-pic-repo-1309997478.cos.ap-chengdu.myqcloud.com/woldier/2023/08/0d9bcd2c420164c800517958601745f2.png)

```java
public interface Spliterator<T> {
    /**
     * If a remaining element exists, performs the given action on it,
     * returning {@code true}; else returns {@code false}.  If this
     * Spliterator is {@link #ORDERED} the action is performed on the
     * next element in encounter order.  Exceptions thrown by the
     * action are relayed to the caller.
     * 如果剩余的元素存在, 那么对该元素执行相应的行为, 并且返回true. 否则返回false. 
     * 如果Spliterator是ORDERED特征的, 那么将会对相遇顺序中的下一个元素执行操作. 
     * @param action The action
     * @return {@code false} if no remaining elements existed
     * upon entry to this method, else {@code true}.
     * @throws NullPointerException if the specified action is null
     */
    boolean tryAdvance(Consumer<? super T> action);

    /**
     * Performs the given action for each remaining element, sequentially in
     * the current thread, until all elements have been processed or the action
     * throws an exception.  If this Spliterator is {@link #ORDERED}, actions
     * are performed in encounter order.  Exceptions thrown by the action
     * are relayed to the caller.
     * 在本线程中顺序的对于剩余的所有元素执行给定的操作,知道所有的元素都被执行了或者是action抛出异常
     * 如果Spliterator是ORDERED特征的, 那么将会对相遇顺序中的下一个元素执行操作.
     * @implSpec
     * The default implementation repeatedly invokes {@link #tryAdvance} until
     * it returns {@code false}.  It should be overridden whenever possible.
     * 默认的实现重复的调用tryAdvance方法知道其返回false. 在可能的情况下,需要将其覆盖.
     * @param action The action
     * @throws NullPointerException if the specified action is null
     */
    default void forEachRemaining(Consumer<? super T> action) {
        do { } while (tryAdvance(action));
    }

    /**
     * If this spliterator can be partitioned, returns a Spliterator
     * covering elements, that will, upon return from this method, not
     * be covered by this Spliterator.
     * 如果本spliterator是可以分块的, 返回的一个Spliterator中的元素包含有本Spliterator
     * 将会不包含的元素
     * <p>If this Spliterator is {@link #ORDERED}, the returned Spliterator
     * must cover a strict prefix of the elements.
     * 如果Spliterator是ORDERED类型的, 那么返回的Spliterator其元素也是严格排序的
     * <p>Unless this Spliterator covers an infinite number of elements,
     * repeated calls to {@code trySplit()} must eventually return {@code null}.
     * Upon non-null return:
     * 除非本Spliterator所覆盖的元素是无穷多的, 重复的(递归)调用trySplit()最终都会返回null
     * <ul>
     * <li>the value reported for {@code estimateSize()} before splitting,
     * must, after splitting, be greater than or equal to {@code estimateSize()}
     * for this and the returned Spliterator; and</li>
     * 拆分之前estimateSize()返回的值必须大于或者等于拆分后estimateSize()方法返回的
     * <li>if this Spliterator is {@code SUBSIZED}, then {@code estimateSize()}
     * for this spliterator before splitting must be equal to the sum of
     * {@code estimateSize()} for this and the returned Spliterator after
     * splitting.</li>
     * 如果说Spliterator是SUBSIZED特征, 那么本Spliterator拆分前的大小必须严格的等于拆分后本Spliterator的大小	    * 与新的Spliterator大小之和
     * </ul>
     *
     * <p>This method may return {@code null} for any reason,
     * including emptiness, inability to split after traversal has
     * commenced, data structure constraints, and efficiency
     * considerations.
     * 本方法在一些情况下会返回null, 包括空,遍历开始后不能够进行拆分, 数据结构的约束, 以及效率考虑.
     * @apiNote
     * An ideal {@code trySplit} method efficiently (without
     * traversal) divides its elements exactly in half, allowing
     * balanced parallel computation.  Many departures from this ideal
     * remain highly effective; for example, only approximately
     * splitting an approximately balanced tree, or for a tree in
     * which leaf nodes may contain either one or two elements,
     * failing to further split these nodes.  However, large
     * deviations in balance and/or overly inefficient {@code
     * trySplit} mechanics typically result in poor parallel
     * performance.
     * 一个理想的trySplit方法高效的将其元素对半拆分(在没有开始遍历之前)那么将可以进行平衡的并行计算.
     * 许多偏离这一理想的做法仍然非常有效. 举个例子, 只有接近的拆分成接近平衡的树,或者对于一个树,
     * 其叶子节点只包括一个或者两个节点. 
     * @return a {@code Spliterator} covering some portion of the
     * elements, or {@code null} if this spliterator cannot be split
     */
    Spliterator<T> trySplit();

    /**
     * Returns an estimate of the number of elements that would be
     * encountered by a {@link #forEachRemaining} traversal, or returns {@link
     * Long#MAX_VALUE} if infinite, unknown, or too expensive to compute.
     *
     * <p>If this Spliterator is {@link #SIZED} and has not yet been partially
     * traversed or split, or this Spliterator is {@link #SUBSIZED} and has
     * not yet been partially traversed, this estimate must be an accurate
     * count of elements that would be encountered by a complete traversal.
     * Otherwise, this estimate may be arbitrarily inaccurate, but must decrease
     * as specified across invocations of {@link #trySplit}.
     *
     * @apiNote
     * Even an inexact estimate is often useful and inexpensive to compute.
     * For example, a sub-spliterator of an approximately balanced binary tree
     * may return a value that estimates the number of elements to be half of
     * that of its parent; if the root Spliterator does not maintain an
     * accurate count, it could estimate size to be the power of two
     * corresponding to its maximum depth.
     *
     * @return the estimated size, or {@code Long.MAX_VALUE} if infinite,
     *         unknown, or too expensive to compute.
     */
    long estimateSize();

    /**
     * Convenience method that returns {@link #estimateSize()} if this
     * Spliterator is {@link #SIZED}, else {@code -1}.
     * @implSpec
     * The default implementation returns the result of {@code estimateSize()}
     * if the Spliterator reports a characteristic of {@code SIZED}, and
     * {@code -1} otherwise.
     *
     * @return the exact size, if known, else {@code -1}.
     */
    default long getExactSizeIfKnown() {
        return (characteristics() & SIZED) == 0 ? -1L : estimateSize();
    }

    /**
     * Returns a set of characteristics of this Spliterator and its
     * elements. The result is represented as ORed values from {@link
     * #ORDERED}, {@link #DISTINCT}, {@link #SORTED}, {@link #SIZED},
     * {@link #NONNULL}, {@link #IMMUTABLE}, {@link #CONCURRENT},
     * {@link #SUBSIZED}.  Repeated calls to {@code characteristics()} on
     * a given spliterator, prior to or in-between calls to {@code trySplit},
     * should always return the same result.
     *
     * <p>If a Spliterator reports an inconsistent set of
     * characteristics (either those returned from a single invocation
     * or across multiple invocations), no guarantees can be made
     * about any computation using this Spliterator.
     *
     * @apiNote The characteristics of a given spliterator before splitting
     * may differ from the characteristics after splitting.  For specific
     * examples see the characteristic values {@link #SIZED}, {@link #SUBSIZED}
     * and {@link #CONCURRENT}.
     *
     * @return a representation of characteristics
     */
    int characteristics();

    /**
     * Returns {@code true} if this Spliterator's {@link
     * #characteristics} contain all of the given characteristics.
     *
     * @implSpec
     * The default implementation returns true if the corresponding bits
     * of the given characteristics are set.
     *
     * @param characteristics the characteristics to check for
     * @return {@code true} if all the specified characteristics are present,
     * else {@code false}
     */
    default boolean hasCharacteristics(int characteristics) {
        return (characteristics() & characteristics) == characteristics;
    }

    /**
     * If this Spliterator's source is {@link #SORTED} by a {@link Comparator},
     * returns that {@code Comparator}. If the source is {@code SORTED} in
     * {@linkplain Comparable natural order}, returns {@code null}.  Otherwise,
     * if the source is not {@code SORTED}, throws {@link IllegalStateException}.
     *
     * @implSpec
     * The default implementation always throws {@link IllegalStateException}.
     *
     * @return a Comparator, or {@code null} if the elements are sorted in the
     * natural order.
     * @throws IllegalStateException if the spliterator does not report
     *         a characteristic of {@code SORTED}.
     */
    default Comparator<? super T> getComparator() {
        throw new IllegalStateException();
    }

    /**
     * Characteristic value signifying that an encounter order is defined for
     * elements. If so, this Spliterator guarantees that method
     * {@link #trySplit} splits a strict prefix of elements, that method
     * {@link #tryAdvance} steps by one element in prefix order, and that
     * {@link #forEachRemaining} performs actions in encounter order.
     *
     * <p>A {@link Collection} has an encounter order if the corresponding
     * {@link Collection#iterator} documents an order. If so, the encounter
     * order is the same as the documented order. Otherwise, a collection does
     * not have an encounter order.
     *
     * @apiNote Encounter order is guaranteed to be ascending index order for
     * any {@link List}. But no order is guaranteed for hash-based collections
     * such as {@link HashSet}. Clients of a Spliterator that reports
     * {@code ORDERED} are expected to preserve ordering constraints in
     * non-commutative parallel computations.
     */
    public static final int ORDERED    = 0x00000010;

    /**
     * Characteristic value signifying that, for each pair of
     * encountered elements {@code x, y}, {@code !x.equals(y)}. This
     * applies for example, to a Spliterator based on a {@link Set}.
     */
    public static final int DISTINCT   = 0x00000001;

    /**
     * Characteristic value signifying that encounter order follows a defined
     * sort order. If so, method {@link #getComparator()} returns the associated
     * Comparator, or {@code null} if all elements are {@link Comparable} and
     * are sorted by their natural ordering.
     *
     * <p>A Spliterator that reports {@code SORTED} must also report
     * {@code ORDERED}.
     *
     * @apiNote The spliterators for {@code Collection} classes in the JDK that
     * implement {@link NavigableSet} or {@link SortedSet} report {@code SORTED}.
     */
    public static final int SORTED     = 0x00000004;

    /**
     * Characteristic value signifying that the value returned from
     * {@code estimateSize()} prior to traversal or splitting represents a
     * finite size that, in the absence of structural source modification,
     * represents an exact count of the number of elements that would be
     * encountered by a complete traversal.
     *
     * @apiNote Most Spliterators for Collections, that cover all elements of a
     * {@code Collection} report this characteristic. Sub-spliterators, such as
     * those for {@link HashSet}, that cover a sub-set of elements and
     * approximate their reported size do not.
     */
    public static final int SIZED      = 0x00000040;

    /**
     * Characteristic value signifying that the source guarantees that
     * encountered elements will not be {@code null}. (This applies,
     * for example, to most concurrent collections, queues, and maps.)
     */
    public static final int NONNULL    = 0x00000100;

    /**
     * Characteristic value signifying that the element source cannot be
     * structurally modified; that is, elements cannot be added, replaced, or
     * removed, so such changes cannot occur during traversal. A Spliterator
     * that does not report {@code IMMUTABLE} or {@code CONCURRENT} is expected
     * to have a documented policy (for example throwing
     * {@link ConcurrentModificationException}) concerning structural
     * interference detected during traversal.
     */
    public static final int IMMUTABLE  = 0x00000400;

    /**
     * Characteristic value signifying that the element source may be safely
     * concurrently modified (allowing additions, replacements, and/or removals)
     * by multiple threads without external synchronization. If so, the
     * Spliterator is expected to have a documented policy concerning the impact
     * of modifications during traversal.
     *
     * <p>A top-level Spliterator should not report both {@code CONCURRENT} and
     * {@code SIZED}, since the finite size, if known, may change if the source
     * is concurrently modified during traversal. Such a Spliterator is
     * inconsistent and no guarantees can be made about any computation using
     * that Spliterator. Sub-spliterators may report {@code SIZED} if the
     * sub-split size is known and additions or removals to the source are not
     * reflected when traversing.
     *
     * @apiNote Most concurrent collections maintain a consistency policy
     * guaranteeing accuracy with respect to elements present at the point of
     * Spliterator construction, but possibly not reflecting subsequent
     * additions or removals.
     */
    public static final int CONCURRENT = 0x00001000;

    /**
     * Characteristic value signifying that all Spliterators resulting from
     * {@code trySplit()} will be both {@link #SIZED} and {@link #SUBSIZED}.
     * (This means that all child Spliterators, whether direct or indirect, will
     * be {@code SIZED}.)
     *
     * <p>A Spliterator that does not report {@code SIZED} as required by
     * {@code SUBSIZED} is inconsistent and no guarantees can be made about any
     * computation using that Spliterator.
     *
     * @apiNote Some spliterators, such as the top-level spliterator for an
     * approximately balanced binary tree, will report {@code SIZED} but not
     * {@code SUBSIZED}, since it is common to know the size of the entire tree
     * but not the exact sizes of subtrees.
     */
    public static final int SUBSIZED = 0x00004000;


   

    
}
```

