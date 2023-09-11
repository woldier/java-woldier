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



另,当我看了一部分集合框架的接口类后,发现有些子接口继承并且重新声明了一些父接口的方法, 非常不解为什么需要重新定义一些方法呢, 这些方法不是已经从父接口中继承得到了吗?

查阅了很多资料,没找到相应的解答,最后询问GPT, 得到了我同意的答案.

> java集合框架中子接口继承父接口并且重新声明父接口中的部分抽象方法,其意义是什么呢
>
> 
>
> ChatGPT
>
> 
>
> 在Java集合框架中，有一些子接口继承了父接口并且重新声明了父接口中的部分抽象方法，这种设计模式有一些特定的意义和用途。以下是一些可能的理由：
>
> 1. 接口的细分：通过继承并重新声明父接口的方法，可以将接口分为更小的功能单元，使接口更加精确地描述其子类型的行为。这有助于提高代码的可维护性和可读性，因为每个子接口都可以更专注于特定的功能领域。
> 2. 自定义实现：子接口可以根据需要重新声明父接口中的方法，以提供特定于实现类的自定义行为。这允许不同的实现类提供不同的方法实现，以满足其独特的需求。
> 3. 向后兼容性：通过重新声明父接口的方法，可以确保现有的代码仍然有效，而不会破坏现有的代码。这使得可以向集合框架添加新的功能而不会中断旧代码的正常运行。
> 4. 强制实现：重新声明父接口的方法可以强制实现类提供对这些方法的具体实现。这对于确保每个实现类都提供了必要的功能非常有用，从而避免了遗漏关键功能的问题。
>
> 总之，子接口继承并重新声明父接口的方法是一种有意义的设计选择，它有助于提高代码的模块化性、可维护性和可扩展性，并允许更好地满足不同实现类的需求。这是Java集合框架中使用的一种常见设计模式。



Collection接口派生出的三个子接口(site:https://blog.csdn.net/m0_67322837/article/details/124322953)

![img](https://woldier-pic-repo-1309997478.cos.ap-chengdu.myqcloud.com/woldier/2023%2F09%2F1574795de91adc3f0f27ccff25a8712a.png)



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

![image-20230816182642345](https://woldier-pic-repo-1309997478.cos.ap-chengdu.myqcloud.com/woldier/2023/08/f24c7e1c738c6c59853e678e986ef16e.png)



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

![image-20230818105818230](https://woldier-pic-repo-1309997478.cos.ap-chengdu.myqcloud.com/woldier/2023/08/f652cf40cb81bdc62b734693453c9b49.png)



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
     * 如果说Spliterator是SUBSIZED特征, 那么本Spliterator拆分前的大小必须严格的等于拆分后本Spliterator的大小	        * 与新的Spliterator大小之和
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
     * 返回将会被便利所遇到的元素数量的估计值, 或者返回int类型的MAX_VALUE(如果说是无限,未知,计算消耗过长的话)
     * 
     * <p>If this Spliterator is {@link #SIZED} and has not yet been partially
     * traversed or split, or this Spliterator is {@link #SUBSIZED} and has
     * not yet been partially traversed, this estimate must be an accurate
     * count of elements that would be encountered by a complete traversal.
     * Otherwise, this estimate may be arbitrarily inaccurate, but must decrease
     * as specified across invocations of {@link #trySplit}.
     * 如果本Spliterator是SIZED并且还仍未遍历任何元素或者是进一步分块, 或者说Spliterator是SUBSIZED
     * 还仍未被部分的遍历, 那么必须准确的估计遍历将遇到的元素.  否则的话, 估计值或许准确度是不可控的, 
     * 但是在调用trySplit时必须按规定减少.
     * @apiNote
     * Even an inexact estimate is often useful and inexpensive to compute.
     * For example, a sub-spliterator of an approximately balanced binary tree
     * may return a value that estimates the number of elements to be half of
     * that of its parent; if the root Spliterator does not maintain an
     * accurate count, it could estimate size to be the power of two
     * corresponding to its maximum depth.
     * 即使时不准确的估计值常常也是有用的并且统计的性能消耗很小. 
     * 举个例子, 一个sub-spliterator的趋于平衡的二叉树可能返回元素数量的估计值, 该值是其父亲的一半.
     * 如果说根Spliterator并没有维护一个精确的计数, 它就会将大小估计为与其最大深度相对应的 2 的幂次。
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
     * 返回当前Spliterator的特征属性, 在trySplit调用之前或者是在trySplit调用的前后,
     * 重复调用本方法的返回值是相同的
     * <p>If a Spliterator reports an inconsistent set of
     * characteristics (either those returned from a single invocation
     * or across multiple invocations), no guarantees can be made
     * about any computation using this Spliterator.
     * 如果一个Spliterator报告了多个特征属性(不管是从一次调用中或者是多次调用), 
     * 那么本Spliterator的任何计算都不能得到保证
     * @apiNote The characteristics of a given spliterator before splitting
     * may differ from the characteristics after splitting.  For specific
     * examples see the characteristic values {@link #SIZED}, {@link #SUBSIZED}
     * and {@link #CONCURRENT}.
     * 给定的spliterator的特征属性在进一步分块之前与之后可能是不同的
     * @return a representation of characteristics
     */
    int characteristics();

    /**
     * Returns {@code true} if this Spliterator's {@link
     * #characteristics} contain all of the given characteristics.
     * 如果说Spliterator的特征属性包含给出的所有特征属性返回true
     * @implSpec
     * The default implementation returns true if the corresponding bits
     * of the given characteristics are set.
     * 默认实现返回true,如果说相应的bit位是被设置的.
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
     * 如果Spliterator源是SORTED并且是通过Comparator来进行比较的,那么返回Comparator.
     * 如果说是SORTED并且排序方式是Comparable natural order,那么返回null. 否则的话, 
     * 如果不是SORTED,抛出IllegalStateException异常
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
     * 特征属性值,表明遇到的元素是有序的. 如果是这样的话, Spliterator 确保方法trySplit根据元素的prefix分块元素
     * 方法tryAdvance根据元素的prefix来进行步进, forEachRemaining根据元素的顺序来进行action. 
     * <p>A {@link Collection} has an encounter order if the corresponding
     * {@link Collection#iterator} documents an order. If so, the encounter
     * order is the same as the documented order. Otherwise, a collection does
     * not have an encounter order.
     * 一个集合如果说相应的迭代器标注了是有序的,那么其Spliterator有着encounter顺序. 否则的话则没有.
     * @apiNote Encounter order is guaranteed to be ascending index order for
     * any {@link List}. But no order is guaranteed for hash-based collections
     * such as {@link HashSet}. Clients of a Spliterator that reports
     * {@code ORDERED} are expected to preserve ordering constraints in
     * non-commutative parallel computations.
     * 对于List来说, Encounter顺序被递增的索引顺序保证. 但是没有任何顺序被基于hash的集合所保证(比如说hashset)
     * 报告 {@code ORDERED} 的 Spliterator 客户端应在非交换并行计算中保留排序约束。
     */
    public static final int ORDERED    = 0x00000010;

    /**
     * Characteristic value signifying that, for each pair of
     * encountered elements {@code x, y}, {@code !x.equals(y)}. This
     * applies for example, to a Spliterator based on a {@link Set}.
     * 本特征值表明, 对于任何一对元素x,y 都有 !x.equals(y).
     * 举个例子, Set会持有此属性
     */
    public static final int DISTINCT   = 0x00000001;

    /**
     * Characteristic value signifying that encounter order follows a defined
     * sort order. If so, method {@link #getComparator()} returns the associated
     * Comparator, or {@code null} if all elements are {@link Comparable} and
     * are sorted by their natural ordering.
     * 本特征值表明, encounter顺序遵循一个被定义的排序. 如果是这样的话, 那么getComparator()方法
     * 返回与之管理的比较器, 或者是null如果所有的元素都是可以比较的,并且根据自然的顺序排序(比如说队列) 
     * <p>A Spliterator that reports {@code SORTED} must also report
     * {@code ORDERED}.
     * 报道SORTED属性那么必须报告出ORDERED
     * @apiNote The spliterators for {@code Collection} classes in the JDK that
     * implement {@link NavigableSet} or {@link SortedSet} report {@code SORTED}.
     * NavigableSet, SortedSet都报告出SORTED.
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

##### 1.2.4.3.2 ArrayListSpliterator

结构如下

![image-20230825103732335](https://woldier-pic-repo-1309997478.cos.ap-chengdu.myqcloud.com/woldier/2023%2F08%2F8b72d143c260e16d197d49494794a938.png)



```java
    /** Index-based split-by-two, lazily initialized Spliterator */
    static final class ArrayListSpliterator<E> implements Spliterator<E> {

        /*
         * If ArrayLists were immutable, or structurally immutable (no
         * adds, removes, etc), we could implement their spliterators
         * with Arrays.spliterator. Instead we detect as much
         * interference during traversal as practical without
         * sacrificing much performance. We rely primarily on
         * modCounts. These are not guaranteed to detect concurrency
         * violations, and are sometimes overly conservative about
         * within-thread interference, but detect enough problems to
         * be worthwhile in practice. 
         * 如果ArrayList是不能相互作用的, 或者是结构不能相互作用的(没有add,remove等),
         * 我们可以通过Arrays.spliterator实现其spliterators. 相反，我们在不影响性能的前提下，
         * 在遍历过程中尽可能多地检测干扰。我们主要依赖modCounts. 这并不能确保检测到并发的原子修改, 
         * 并且对线程内干扰的也过于保守, 但是实际使用中足够检测问题.
         * To carry this out, we (1) lazily
         * initialize fence and expectedModCount until the latest
         * point that we need to commit to the state we are checking
         * against; thus improving precision.  (This doesn't apply to
         * SubLists, that create spliterators with current non-lazy
         * values).  
         * 为了实现, 我们(1) 懒惰的初始化fence和expectedModCount直到最后的需要提交我们正在检查状态的point分配
         * (2) We perform only a single
         * ConcurrentModificationException check at the end of forEach
         * (the most performance-sensitive method). When using forEach
         * (as opposed to iterators), we can normally only detect
         * interference after actions, not before. Further
         * CME-triggering checks apply to all other possible
         * violations of assumptions for example null or too-small
         * elementData array given its size(), that could only have
         * occurred due to interference.  This allows the inner loop
         * of forEach to run without any further checks, and
         * simplifies lambda-resolution. While this does entail a
         * number of checks, note that in the common case of
         * list.stream().forEach(a), no checks or other computation
         * occur anywhere other than inside forEach itself.  The other
         * less-often-used methods cannot take advantage of most of
         * these streamlinings.
         * 在forEach结束之后我们进行了唯一一次ConcurrentModificationException检查. 
         * 当使用forEach时候(与Iterator中不同), 我们通常只在action之后检测干扰情况, 而不是之前.
         * 进一步的 CME 触发检查适用于所有其他可能违反假设的情况，例如，在元素数据数组的 size() 条件下出现空
         * 或过小的情况，而这些情况只可能是由于干扰造成的。
         * 这使得forEach的内部循环在没有更进一步的检查的情况下运行, 并且简化lambda解析. 
         * 虽然这确实需要进行一些检查，但请注意，在 list.stream().forEach(a) 的常见情况下，
         * 除了 forEach 本身之外，不会在其他地方进行检查或其他计算。 
         * 其他不常用的方法则无法利用这些精简的大部分功能。
         */

        private final ArrayList<E> list; //关联list
        private int index; // current index, modified on advance/split
        private int fence; // -1 until used; then one past last index
        private int expectedModCount; // initialized when fence set

        /** Create new spliterator covering the given  range */
        ArrayListSpliterator(ArrayList<E> list, int origin, int fence,
                             int expectedModCount) {
            this.list = list; // OK if null unless traversed
            this.index = origin;
            this.fence = fence;
            this.expectedModCount = expectedModCount;
        }

        private int getFence() { // initialize fence to size on first use
            int hi; // (a specialized variant appears in method forEach)
            ArrayList<E> lst;
            if ((hi = fence) < 0) { // fence未初始化的时候成立，说明此spliterator是root
                if ((lst = list) == null) //如果list是空
                    hi = fence = 0; //设置hi 和fence等于0
                else { //list不是空 
                    expectedModCount = lst.modCount; //设置modCount
                    hi = fence = lst.size; //设置长度
                }
            }
            return hi;
        }

        public ArrayListSpliterator<E> trySplit() {  //尝试拆分
            int hi = getFence(), lo = index, mid = (lo + hi) >>> 1; //得到中间位置，用位操作防止溢出
            return (lo >= mid) ? null : // divide range in half unless too small
                new ArrayListSpliterator<E>(list, lo, index = mid, //重新设置index
                                            expectedModCount);
        }

        public boolean tryAdvance(Consumer<? super E> action) {
            if (action == null) //判断传入的func是否是空
                throw new NullPointerException();
            int hi = getFence(), i = index; //暂存栅栏和索引值
            if (i < hi) { //判断索引是否超过fence
                index = i + 1; //索引加一
                @SuppressWarnings("unchecked") E e = (E)list.elementData[i]; //获取对于位置的元素
                action.accept(e); //执行函数
                if (list.modCount != expectedModCount) //检查并发修改
                    throw new ConcurrentModificationException();
                return true;
            }
            return false;
        }

        public void forEachRemaining(Consumer<? super E> action) {
            int i, hi, mc; // hoist accesses and checks from loop
            ArrayList<E> lst; Object[] a; 
            if (action == null) //判断action是否为空
                throw new NullPointerException();
            if ((lst = list) != null && (a = lst.elementData) != null) { 
              //list不为空，并且list数组不为空
                if ((hi = fence) < 0) { //如果将fence的值赋值给hi，然后判断hi的值是否是小于零
                  // fence小于零的情况是还未初始化（因为是了lazy ini）
                    mc = lst.modCount;  //赋值modifycount
                    hi = lst.size; //设置li的最大索引
                }
                else
                    mc = expectedModCount;  // 暂存expectedModCount
                if ((i = index) >= 0 && (index = hi) <= a.length) { 
                  //当前索引大于等于零 ，然后将hi的值赋值给index 并且赋之后的index值小于数组a的长度
                    for (; i < hi; ++i) {
                        @SuppressWarnings("unchecked") E e = (E) a[i];
                        action.accept(e);
                    }
                    if (lst.modCount == mc) //执行for之后 判断mc是否发生了改变
                        return;
                }
            }
            throw new ConcurrentModificationException(); //抛出异常
        }

        public long estimateSize() {
            return (long) (getFence() - index);
        }

        public int characteristics() {
            return Spliterator.ORDERED | Spliterator.SIZED | Spliterator.SUBSIZED;
        }
    }
```

#### 1.2.4.4 实现的List接口

![image-20230826102353764](https://woldier-pic-repo-1309997478.cos.ap-chengdu.myqcloud.com/woldier/2023/08/3ce0daa97b04e87b935deceac9e05ecc.png)

实现的方法，有的来自于AbstractCollection，有的来自于AbstractList

接下来，我们一个一个分析方法，查看继承自哪个类

##### 1.2.4.4.1 size

![image-20230826103628973](https://woldier-pic-repo-1309997478.cos.ap-chengdu.myqcloud.com/woldier/2023/08/39a4db62caeaabbc21898f7b998d44b7.png)

本方法继承自AbstractCollection，并且将其进行了重写

```java
     public abstract int size();
```

```java
    

		/**
     * Returns the number of elements in this list.
     *
     * @return the number of elements in this list
     */
		@Override
    public int size() {
        return size;
    }
```

##### 1.2.4.4.2 isEmpty方法

![image-20230826120154681](https://woldier-pic-repo-1309997478.cos.ap-chengdu.myqcloud.com/woldier/2023/08/0f9cba848ccfe37f55c8b2091cf6c96e.png)

isEmpty方法继承并且重写了AbstractCollection中的方法

先看一下父类中的方法

```java
public boolean isEmpty() {
        return size() == 0;
    }
```

在看一下重写的方法

```java
    /**
     * Returns <tt>true</tt> if this list contains no elements.
     *
     * @return <tt>true</tt> if this list contains no elements
     */
    public boolean isEmpty() {
        return size == 0;
    }
```

发现完全是一致的，就是copy了父类的方法



##### 1.2.4.4.3 contains

![image-20230826120453456](https://woldier-pic-repo-1309997478.cos.ap-chengdu.myqcloud.com/woldier/2023/08/f1739236edfdc4c700af8556cb370735.png)



先查看一下父类中的方法，本方法在AbstractCollection中介绍过了（算法思想是通过迭代器去遍历元素）

```java
  public boolean contains(Object o) {
        Iterator<E> it = iterator();
        if (o==null) {
            while (it.hasNext())
                if (it.next()==null)
                    return true;
        } else {
            while (it.hasNext())
                if (o.equals(it.next()))
                    return true;
        }
        return false;
    }
```



而ArrayList的实现类中则是重写了逻辑， 通过调用indexOf方法查找元素

```java
public boolean contains(Object o) {
        return indexOf(o) >= 0;
    }
```

对一indexOf方法的介绍，可以继续看List接口的方法实现



##### 1.2.4.4.4 indexOf

![image-20230826120955473](https://woldier-pic-repo-1309997478.cos.ap-chengdu.myqcloud.com/woldier/2023/08/0f1b98f610d0c3c46f5665a1826b9768.png)

indexOf方法继承自AbstractList

先看一下AbstractList的实现(本方法在之前已经介绍过了，因此不做过多说明，可以查看对应的章节)

```java
 public int indexOf(Object o) {
        ListIterator<E> it = listIterator();
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
```

再看看ArrayList的实现

```java
    /**
     * Returns the index of the first occurrence of the specified element
     * in this list, or -1 if this list does not contain the element.
     * More formally, returns the lowest index <tt>i</tt> such that
     * <tt>(o==null&nbsp;?&nbsp;get(i)==null&nbsp;:&nbsp;o.equals(get(i)))</tt>,
     * or -1 if there is no such index.
     */
    public int indexOf(Object o) {
        if (o == null) { //判断空
            for (int i = 0; i < size; i++)
                if (elementData[i]==null)
                    return i;
        } else { //非空
            for (int i = 0; i < size; i++)
                if (o.equals(elementData[i]))
                    return i;
        }
        return -1;
    }
```

##### 1.2.4.4.5 LastIndexOf

![image-20230826121458695](https://woldier-pic-repo-1309997478.cos.ap-chengdu.myqcloud.com/woldier/2023/08/4e9c726ea7122117923338363cfe20d2.png)

本方法继承自AbstractList并且做了重写

先看看父类的逻辑(和indexOf相同，都是基于迭代器完成的)

```java
    public int lastIndexOf(Object o) {
        ListIterator<E> it = listIterator(size());
        if (o==null) {
            while (it.hasPrevious())
                if (it.previous()==null)
                    return it.nextIndex();
        } else {
            while (it.hasPrevious())
                if (o.equals(it.previous()))
                    return it.nextIndex();
        }
        return -1;
    }
```

再查看一下ArrayList的实现(于indexOf实现思想差不多，不过是从尾向头部扫描)

```java
  public int lastIndexOf(Object o) {
        if (o == null) {
            for (int i = size-1; i >= 0; i--)
                if (elementData[i]==null)
                    return i;
        } else {
            for (int i = size-1; i >= 0; i--)
                if (o.equals(elementData[i]))
                    return i;
        }
        return -1;
    }
```

##### 1.2.4.4.6 toArray

![image-20230826122007740](https://woldier-pic-repo-1309997478.cos.ap-chengdu.myqcloud.com/woldier/2023/08/82da993bc2a10d100e30dad6d8a4f7b9.png)

继承自AbstractCollection

先查看一下父类的实现

父类做的事情是先获取到迭代器，然后把迭代器中的元素拷贝到对应的对象数组中。

```java
public Object[] toArray() {
    // Estimate size of array; be prepared to see more or fewer elements
    Object[] r = new Object[size()];
    Iterator<E> it = iterator();
    for (int i = 0; i < r.length; i++) {
        if (! it.hasNext()) // fewer elements than expected
            return Arrays.copyOf(r, i);
        r[i] = it.next();
    }
    return it.hasNext() ? finishToArray(r, it) : r;
}
```

而对于AyyayList来说，内部维护的就是一个数组。 那么通过迭代器，创建数组的方式是低效的。

完全可以直接调用`System.ArrayCopy`方法完成上述任务

接下来看一下ArrayList的实现非常的简洁明了。

但是需要注意的是，如果说ArrayList中保存的元素类型不是基本类型，那么拷贝的数组中的对象都是指针引用。

因此在拷贝的数组中修改对象中的属性，会对源ArrayList产生影响

```java
public Object[] toArray() {
    return Arrays.copyOf(elementData, size);
}
```

##### 1.2.4.4.7 toArray(T[] a)

![image-20230826122633633](https://woldier-pic-repo-1309997478.cos.ap-chengdu.myqcloud.com/woldier/2023/08/82da993bc2a10d100e30dad6d8a4f7b9.png)

toArray方法的重载

先查看下父类的实现。

```java
public <T> T[] toArray(T[] a) {
    // Estimate size of array; be prepared to see more or fewer elements
    int size = size(); //得到集合的容量
    T[] r = a.length >= size ? a : //如果a的长度大于或者等于当前集合的容量，那么就使用a作为返回集
              (T[])java.lang.reflect.Array //否则创建一个可以容纳返回集size的数组
              .newInstance(a.getClass().getComponentType(), size);
    Iterator<E> it = iterator();

    for (int i = 0; i < r.length; i++) {
        if (! it.hasNext()) { // fewer elements than expected  当前的元素数量比期望中的小
            if (a == r) { //如果说a与r是同一个数组，说明并没有因为a的容量不足而创建一个新的数组
                r[i] = null; // null-terminate
            } else if (a.length < i) { //能够进入本if判断，说明a容量不足，创建了一个新的。并且当前遍历到的指针索引已经超过了a的最大长度。
                return Arrays.copyOf(r, i); //那么进行trim操作，然后返回
            } else { //能够进入到这说明r不是a，并且当前遍地到的索引还没有超过a的最大长度
                System.arraycopy(r, 0, a, 0, i);//数组拷贝，将r中的元素拷贝到a中
                if (a.length > i) {//判断a的索引值是否小于i，如果是，那么设置索引i的元素为null
                    a[i] = null;
                }
            }
            return a;
        }
        r[i] = (T)it.next();
    }
    // more elements than expected
    return it.hasNext() ? finishToArray(r, it) : r; //处理当前元素比预期多的情况
}
```

再看一下子类的实现

```java
 public <T> T[] toArray(T[] a) {
        if (a.length < size) //如果说给的的数组a的最大长度小于当前的大小
            // Make a new array of a's runtime type, but my contents:
            return (T[]) Arrays.copyOf(elementData, size, a.getClass());
   //能够执行接下来的代码说明a的最大长度大于或者等于size
        System.arraycopy(elementData, 0, a, 0, size); //数组拷贝
        if (a.length > size) //判断a的长度大于size的情况
            a[size] = null;
        return a;
    }
```

##### 1.2.4.4.8 get

![image-20230826130154113](https://woldier-pic-repo-1309997478.cos.ap-chengdu.myqcloud.com/woldier/2023/08/f1da186601eacdcc4a5924bc29a2cdb9.png)

先看看父类的方法，父类定义为抽象方法，需要子类实现

```java
    /**
     *
     * @throws IndexOutOfBoundsException {@inheritDoc}
     */
    abstract public E get(int index);
```

子类实现

```java
    public E get(int index) {
        rangeCheck(index);//检查越界

        return elementData(index); //对访问数组元素的方法封装
    }
```

##### 1.2.4.4.9 set(int index, E element)

![image-20230827101537000](https://woldier-pic-repo-1309997478.cos.ap-chengdu.myqcloud.com/woldier/2023/08/a6733fcdcd4c40df4cc7810335109e91.png)

本方法是List接口定义的。是List独有的方法，List接口中的定义如下

```java
E set(int index, E element);
```

紧接着，在AbstractList中对其进行了实现，但是默认的实现是直接抛出UnSupported异常

![image-20230827102013553](https://woldier-pic-repo-1309997478.cos.ap-chengdu.myqcloud.com/woldier/2023/08/38b223308c5a1a7ed7ff350685c735a9.png)

```java
    public E set(int index, E element) {
        throw new UnsupportedOperationException();
    }
```

再来看看ArrayList的实现

```java
    public E set(int index, E element) {
        rangeCheck(index); //检查越界

        E oldValue = elementData(index); //得到旧的索引位置元素值
        elementData[index] = element; //用新的元素代替
        return oldValue; //返回旧的值
    }
```

##### 1.2.4.4.10 add(int index, E element)

![image-20230828153604214](https://woldier-pic-repo-1309997478.cos.ap-chengdu.myqcloud.com/woldier/2023/08/8ebf0838e61ef15116b6bc3d5df8dc79.png)

实现了List接口,同时继承,并且重写了AbstractList的方法

```java
    void add(int index, E element); //List接口中的方法
```

```java
public void add(int index, E element) {
        throw new UnsupportedOperationException(); //AbstractList的方法抛出异常
    }
```

再来看看ArrayList的实现

```java
  public void add(int index, E element) {
        rangeCheckForAdd(index); //检查越界

        ensureCapacityInternal(size + 1);  // Increments modCount!! 动态扩容，并且会让modCount加一
        System.arraycopy(elementData, index, elementData, index + 1,
                         size - index);  //元素右移腾出位置
        elementData[index] = element;
        size++;
    }
```



```java
 private void ensureCapacityInternal(int minCapacity) {
        ensureExplicitCapacity(calculateCapacity(elementData, minCapacity));
    }

   private static int calculateCapacity(Object[] elementData, int minCapacity) {//计算容量
        if (elementData == DEFAULTCAPACITY_EMPTY_ELEMENTDATA) { 
          // 如果当前的数组引用是DEFAULTCAPACITY_EMPTY_ELEMENTDATA， 那么说明当前为空数组
            return Math.max(DEFAULT_CAPACITY, minCapacity);
          //返回默认数组大小与传入的minCapacity较大的值
        }
        return minCapacity;
    }

  private void ensureExplicitCapacity(int minCapacity) {
        modCount++; //修改计数加一

        // overflow-conscious code
        if (minCapacity - elementData.length > 0) //如果传入的minCapacity大于当前数组的长度那么执行扩容
            grow(minCapacity);
    }

   private void grow(int minCapacity) {
        // overflow-conscious code
        int oldCapacity = elementData.length;//得到旧的容量
        int newCapacity = oldCapacity + (oldCapacity >> 1); //计算新的容量，（可能存在溢出的情况）
        if (newCapacity - minCapacity < 0) //如果说newCapacity减去minCapacity小于0，说明已经溢出了
            newCapacity = minCapacity; // 设置newCapacity为minCapacity
        if (newCapacity - MAX_ARRAY_SIZE > 0) //如果newCapacity没有溢出，并且超过了预设的MAX_ARRAY_SIZE
            newCapacity = hugeCapacity(minCapacity);
        // minCapacity is usually close to size, so this is a win:
        elementData = Arrays.copyOf(elementData, newCapacity);
    }


    private static int hugeCapacity(int minCapacity) {
        if (minCapacity < 0) // overflow
            throw new OutOfMemoryError();
        return (minCapacity > MAX_ARRAY_SIZE) ?  //如果没有溢出,且比MAX_ARRAY_SIZE大,那么返回最大整数,否则返回MAX_ARRAY_SIZE
            Integer.MAX_VALUE :
            MAX_ARRAY_SIZE;
    }
```



##### 1.2.4.4.11 add(E e)

![image-20230828162413312](https://woldier-pic-repo-1309997478.cos.ap-chengdu.myqcloud.com/woldier/2023/08/e84d541ead51d57060c8bcd8383cb0fa.png)

在List接口中查看该方法,可以发现它又继承自Collection接口,只是重新声明了一次

![image-20230828162617297](https://woldier-pic-repo-1309997478.cos.ap-chengdu.myqcloud.com/woldier/2023/08/e9ec6df4c419242bf5839dcafc1fa81a.png)

```java
    boolean add(E e);
```

紧接着看看AbstractList中的add(E e)方法:

![image-20230828162732108](https://woldier-pic-repo-1309997478.cos.ap-chengdu.myqcloud.com/woldier/2023/08/e8972016c5adcad80c0f6c23c3f15695.png)

该方法实现了List接口,同时继承并且重写了AbstractCollection中的add方法

对于AbstractCollection中的add方法 其实现了Collection接口 ,默认抛出异常.

![image-20230828162849475](https://woldier-pic-repo-1309997478.cos.ap-chengdu.myqcloud.com/woldier/2023/08/128b5458f1f18fce3062b1a901f2d6f0.png)

接下来看看AbstractCollection的实现, 其思路是直接调用add(int index, E element).

```java
   public boolean add(E e) {
        add(size(), e);
        return true;
    }
```



最后来看看ArrayList 的实现 ,由于ArrayList 内部维护的是一个数组,因此 可以直接访问对应位置元素.

```java
    public boolean add(E e) {
        ensureCapacityInternal(size + 1);  // Increments modCount!! 动态扩容
        elementData[size++] = e;
        return true;
    }
```



##### 1.2.4.4.12 remove(int index)

![image-20230828163412857](https://woldier-pic-repo-1309997478.cos.ap-chengdu.myqcloud.com/woldier/2023/08/1a5f874ef5951637dae8b17e323037ec.png)



remove方法实现了List接口的方法,并且实现了AbstractList定义的抽象方法(这里是idea显示错误,其实AbstractList已经实现了该方法). 



先去看看List接口,相对于继承的Collection中的众多方法 该方法是List接口拓展的

```java
    E remove(int index);
```

然后再来看看AbstractList中的方法, 该方法实现了List接口的remove方法, 默认实现抛出异常

![image-20230828163919044](https://woldier-pic-repo-1309997478.cos.ap-chengdu.myqcloud.com/woldier/2023/08/e8c2dfea131e0eb820991de52996ead2.png)

```java
 public E remove(int index) {
        throw new UnsupportedOperationException();
    }
```



最后,来看看ArrayList的实现

```java
    public E remove(int index) {
        rangeCheck(index); //检查index合法性

        modCount++; // modify次数增加
        E oldValue = elementData(index); //得到待删除的值

        int numMoved = size - index - 1; //计算待移动的元素个数
        if (numMoved > 0) //如果待移动的元素个数大于零,那么做数组拷贝
            System.arraycopy(elementData, index+1, elementData, index,
                             numMoved);
        elementData[--size] = null; // clear to let GC do its work 如果不做这件事,那么数组中对对象的引用依旧是存在的,那么不会被垃圾回收

        return oldValue;
    }
```



##### 1.2.4.4.13 remove(Object o)

![image-20230828165241379](https://woldier-pic-repo-1309997478.cos.ap-chengdu.myqcloud.com/woldier/2023/08/d2005d163d1188b9b80aa8236f040070.png)

先看看List接口中的方法

![image-20230828165650768](https://woldier-pic-repo-1309997478.cos.ap-chengdu.myqcloud.com/woldier/2023/08/9cf4f365e02bf8524628cd9375e42c5b.png)

可以发现,其继承了Collection中的remove方法,并且重写声明了该方法.



再来看看AbstractList中的方法,其实现了List的接口,那么其继承结构与前面类似.

AbstractList中的算法思想是使用迭代器, 前面的章节已经介绍过,因此这么不在做过多的赘述;

```java
    public boolean remove(Object o) {
        Iterator<E> it = iterator();
        if (o==null) {
            while (it.hasNext()) {
                if (it.next()==null) {
                    it.remove();
                    return true;
                }
            }
        } else {
            while (it.hasNext()) {
                if (o.equals(it.next())) {
                    it.remove();
                    return true;
                }
            }
        }
        return false;
    }
```

最后看一下ArrayList中的实现

```java
    public boolean remove(Object o) {
        if (o == null) {
            for (int index = 0; index < size; index++)
                if (elementData[index] == null) {
                    fastRemove(index);
                    return true;
                }
        } else {
            for (int index = 0; index < size; index++)
                if (o.equals(elementData[index])) {
                    fastRemove(index);
                    return true;
                }
        }
        return false;
    }

    /*
     * Private remove method that skips bounds checking and does not
     * return the value removed.
     */
    private void fastRemove(int index) {
        modCount++;
        int numMoved = size - index - 1;
        if (numMoved > 0)
            System.arraycopy(elementData, index+1, elementData, index,
                             numMoved);
        elementData[--size] = null; // clear to let GC do its work
    }
```



##### 1.2.4.4.14 clear()

对于该方法Collection,AbstractCollection,List,AbstractList接口都定义或者是实现了该方法

先看看Collection方法

```java
public interface Collection<E> extends Iterable<E> {
    void clear();
```

接着看看AbstractCollection

```java
public abstract class AbstractCollection<E> implements Collection<E> {
    public void clear() {
        Iterator<E> it = iterator();
        while (it.hasNext()) {
            it.next();
            it.remove();
        }
    }

```

紧接着是继承了Collection接口的List

```java
public interface List<E> extends Collection<E> {
    void clear();
```

然后是继承了AbstractCollection抽象类和List接口的AbstractList

```java
public abstract class AbstractList<E> extends AbstractCollection<E> implements List<E> {
   public void clear() {
        removeRange(0, size());
    }
```

最后则是继承了AbstractList并且实现了List接口的实现类

```java

public class ArrayList<E> extends AbstractList<E>
        implements List<E>, RandomAccess, Cloneable, java.io.Serializable
{
    /**
     * Removes all of the elements from this list.  The list will
     * be empty after this call returns.
     */
    public void clear() {
        modCount++; 

        // clear to let GC do its work
        for (int i = 0; i < size; i++)
            elementData[i] = null;

        size = 0;
    }

```



##### 1.2.4.4.15 addAll(Collection<? extends E> c)



![image-20230828173449566](https://woldier-pic-repo-1309997478.cos.ap-chengdu.myqcloud.com/woldier/2023/08/a782c55e56de501d35dfe2de315a80b1.png)

实现了List的addAll方法,List中的addAll方法又继承自Collection. List接口中只是做了显式的定义. 

![image-20230828173830307](https://woldier-pic-repo-1309997478.cos.ap-chengdu.myqcloud.com/woldier/2023/08/a91c6eecfe29f4c617f8ffd1a83fea52.png)

然后看一下AbstractCollection 内部循环调用的add(e)方法

```java
public abstract class AbstractCollection<E> implements Collection<E> {
  public boolean addAll(Collection<? extends E> c) {
        boolean modified = false;
        for (E e : c)
            if (add(e))
                modified = true;
        return modified;
    }
```

最后看一下ArrayList的实现

```java
    public boolean add(E e) {
        ensureCapacityInternal(size + 1);  // Increments modCount!!  动态扩容
        elementData[size++] = e; //根据数组随机访存的特征进行了优化
        return true;
    }
```



##### 1.2.4.4.16 addAll(int index, Collection<? extends E> c)

![image-20230831090109454](https://woldier-pic-repo-1309997478.cos.ap-chengdu.myqcloud.com/woldier/2023/08/d65784116b0265de160bb7709b9b8f3e.png)

本方法是List扩展的方法而不是从Collection接口继承的, 因此未List独有的.

```java
public interface List<E> extends Collection<E> {
    boolean addAll(int index, Collection<? extends E> c);
```

AbstractList实现了List接口, 给出默认的实现(基于迭代器)

```java
public abstract class AbstractList<E> extends AbstractCollection<E> implements List<E> {
    public boolean addAll(int index, Collection<? extends E> c) {
        rangeCheckForAdd(index);
        boolean modified = false;
        for (E e : c) {
            add(index++, e);
            modified = true;
        }
        return modified;
    }
```



最后,再来看看ArrayList中的方法.

```java
    public boolean addAll(int index, Collection<? extends E> c) {
        rangeCheckForAdd(index); //检查索引的合法性

        Object[] a = c.toArray(); //转为数组
        int numNew = a.length; 
        ensureCapacityInternal(size + numNew);  // Increments modCount

        int numMoved = size - index; //得到需要移动的元素的数量
        if (numMoved > 0) //移动元素
            System.arraycopy(elementData, index, elementData, index + numNew,
                             numMoved);

        System.arraycopy(a, 0, elementData, index, numNew); //拷贝元素
        size += numNew;
        return numNew != 0;
    }
```



##### 1.2.4.4.17 removeAll(Collection<?> c)

![image-20230831112800900](https://woldier-pic-repo-1309997478.cos.ap-chengdu.myqcloud.com/woldier/2023/08/748161aeab70db97a8d52c0ae8a75ed1.png)

该方法实现了List接口

![image-20230831112911187](https://woldier-pic-repo-1309997478.cos.ap-chengdu.myqcloud.com/woldier/2023/08/db7ac7fc86f57c83d1565e60ce66c7ee.png)

而List接口中的RemoveAll方法又继承自Collection中的方法,List只是对其进行了再次的声明.



对于AbstractCollection 其是西安了Collection中相应的接口, 代码逻辑在 1.1已经介绍过了,这里不再赘述

```java
    public boolean removeAll(Collection<?> c) {
        Objects.requireNonNull(c);
        boolean modified = false;
        Iterator<?> it = iterator();
        while (it.hasNext()) {
            if (c.contains(it.next())) {
                it.remove();
                modified = true;
            }
        }
        return modified;
    }
```



最后看下ArrayList的实现

```java
    public boolean removeAll(Collection<?> c) {
        Objects.requireNonNull(c);
        return batchRemove(c, false);
    }
```



```java
private boolean batchRemove(Collection<?> c, boolean complement) {
        final Object[] elementData = this.elementData; //得到内部维护的数组
        int r = 0, w = 0; //读指针与写指针
        boolean modified = false;
        try {
            for (; r < size; r++)  //读指针每次循环增加
                //如果c中不包含elementData中的元素,那么写指针也会移动
                if (c.contains(elementData[r]) == complement)
                    elementData[w++] = elementData[r];
        } finally {
            // Preserve behavioral compatibility with AbstractCollection,
            // even if c.contains() throws.
            if (r != size) { //有可能剩余的contains抛出了异常那么需要考虑后续元素的保留
                System.arraycopy(elementData, r,
                                 elementData, w,
                                 size - r);
                w += size - r;
            }
            if (w != size) { //如果w不等于size 说明有元素被删除了 ,那么需要清除后续元素的引用
                // clear to let GC do its work
                for (int i = w; i < size; i++)
                    elementData[i] = null;
                modCount += size - w;
                size = w;
                modified = true;
            }
        }
        return modified;
    }
```

##### 1.2.4.4.18 retainAll(Collection<?> c)

![image-20230831142705809](https://woldier-pic-repo-1309997478.cos.ap-chengdu.myqcloud.com/woldier/2023/08/8928c4ecead4e3061b1d52712c2ac6d8.png)

实现了List的接口,而list接口中的`retainAll(Collection<?> c)`方法继承自Collection接口,只是List接口将该方法重新定义了

![image-20230831143829078](https://woldier-pic-repo-1309997478.cos.ap-chengdu.myqcloud.com/woldier/2023/08/3a317d9dfdcba0b713488bed4f61ed20.png)



重写并继承了AbstractCollection的方法

```java
 public boolean retainAll(Collection<?> c) {
        Objects.requireNonNull(c);
        boolean modified = false;
        Iterator<E> it = iterator();
        while (it.hasNext()) {
            if (!c.contains(it.next())) {
                it.remove();
                modified = true;
            }
        }
        return modified;
    }
```

最后看下ArrayList中的方法

也是调用的batchRemove 不过第二个参数传入的true,因为之前已经介绍过了batchRemove因此这里不再赘述

```java
   public boolean retainAll(Collection<?> c) {
        Objects.requireNonNull(c);
        return batchRemove(c, true);
    }
```

##### 1.2.4.4.19 listIterator && iterator

返回不同迭代器的方法

因此放在一起介绍

```java
public ListIterator<E> listIterator(int index) {
        if (index < 0 || index > size)
            throw new IndexOutOfBoundsException("Index: "+index);
        return new ListItr(index);
    }

 public ListIterator<E> listIterator() {
        return new ListItr(0);
    }

 public Iterator<E> iterator() {
        return new Itr();
    }
```

就是返回了内部的ListItr.class和Itr.class

##### 1.2.4.4.20 subList(int fromIndex, int toIndex)

```java
public List<E> subList(int fromIndex, int toIndex) {
    *subListRangeCheck*(fromIndex, toIndex, size);
    return new SubList(this, 0, fromIndex, toIndex);
}
```



![image-20230831155713868](https://woldier-pic-repo-1309997478.cos.ap-chengdu.myqcloud.com/woldier/2023/08/7213f7377ca59350ba4682d4cc9604ba.png)

#### 1.2.4.5 重写的Object中的方法

##### 1.2.4.5.1 clone()

```java
    public Object clone() {
        try {
            ArrayList<?> v = (ArrayList<?>) super.clone();
            v.elementData = Arrays.copyOf(elementData, size);
            v.modCount = 0;
            return v;
        } catch (CloneNotSupportedException e) {
            // this shouldn't happen, since we are Cloneable
            throw new InternalError(e);
        }
    }
```



#### 1.2.4.6 重写的AbstractList中的方法

##### 1.2.5.6.1 removeRange

AbstractListzh中定义的方法,通过ListItr来完成

```java
   protected void removeRange(int fromIndex, int toIndex) {
        ListIterator<E> it = listIterator(fromIndex);
        for (int i=0, n=toIndex-fromIndex; i<n; i++) {
            it.next();
            it.remove();
        }
    }
```

再来看看重写的,直接操作数组

```java
    protected void removeRange(int fromIndex, int toIndex) {
        modCount++;
        int numMoved = size - toIndex;
        System.arraycopy(elementData, toIndex, elementData, fromIndex,
                         numMoved);

        // clear to let GC do its work
        int newSize = size - (toIndex-fromIndex);
        for (int i = newSize; i < size; i++) {
            elementData[i] = null;
        }
        size = newSize;
    }
```

#### 1.2.5.7 重写的Iterable中的方法

##### 1.2.5.7.1 forEach

```java
    @Override
    public void forEach(Consumer<? super E> action) {
        Objects.requireNonNull(action); //判断action非空
        final int expectedModCount = modCount; //得到modify的次数
        @SuppressWarnings("unchecked")
        final E[] elementData = (E[]) this.elementData; //得到内部维护的数组并且进行类型转换
        final int size = this.size; //得到size
        for (int i=0; modCount == expectedModCount && i < size; i++) { //遍历所有的元素,并且监控修改
            action.accept(elementData[i]);
        }
        if (modCount != expectedModCount) {
            throw new ConcurrentModificationException();
        }
    }
```



#### 1.2.5.8 重写的Collection中的方法

![image-20230902153008149](https://woldier-pic-repo-1309997478.cos.ap-chengdu.myqcloud.com/woldier/2023/09/915a17b8b1051e067470cc899bcde6dd.png)

##### 1.2.5.8.1 spliterator 

```java
@Override
    public Spliterator<E> spliterator() {
        return new ArrayListSpliterator<>(this, 0, -1, 0);
    }
```

##### 1.2.5.8.2 removeIf

```java
    @Override
    public boolean removeIf(Predicate<? super E> filter) { //根据条件过滤元素
        Objects.requireNonNull(filter);
        // figure out which elements are to be removed
        // any exception thrown from the filter predicate at this stage
        // will leave the collection unmodified
        int removeCount = 0; //计数
        final BitSet removeSet = new BitSet(size); //用于记录那些索引的元素被移除了
        final int expectedModCount = modCount; //记录modify计数
        final int size = this.size; //得到当前的size大小
        for (int i=0; modCount == expectedModCount && i < size; i++) {//判断for循环的结束条件中有modCount == expectedModCount 也就是说,当出现并发修改时,立马就会退出
            @SuppressWarnings("unchecked")
            final E element = (E) elementData[i]; //强转对象
            if (filter.test(element)) { //是否满足预测条件
                removeSet.set(i); //设置对应的bit
                removeCount++;
            }
        }
        if (modCount != expectedModCount) { //再次检查并发修改
            throw new ConcurrentModificationException();
        }

        // shift surviving elements left over the spaces left by removed elements
        final boolean anyToRemove = removeCount > 0;
        if (anyToRemove) { //如果存在被移除的
            final int newSize = size - removeCount; //计算新的大小
            for (int i=0, j=0; (i < size) && (j < newSize); i++, j++) {
                i = removeSet.nextClearBit(i);//得到下一个未被修改过的索引
                elementData[j] = elementData[i]; 
            }
            for (int k=newSize; k < size; k++) { //清理后续的元素引用
                elementData[k] = null;  // Let gc do its work
            }
            this.size = newSize;//设置新的大小
            if (modCount != expectedModCount) {
                throw new ConcurrentModificationException();
            }
            modCount++;
        }

        return anyToRemove;
    }
```



#### 1.2.5.9 重写的List的方法

![image-20230904152613898](https://woldier-pic-repo-1309997478.cos.ap-chengdu.myqcloud.com/woldier/2023%2F09%2F6a293321003588beef6ee235088e47bb.png)

##### 1.2.5.9.1 replaceAll

重写了List接口特有的两个接口方法

```java
    @Override
    @SuppressWarnings("unchecked")
    public void replaceAll(UnaryOperator<E> operator) {
        Objects.requireNonNull(operator);
        final int expectedModCount = modCount; //记录修改次数
        final int size = this.size; //大小
        for (int i=0; modCount == expectedModCount && i < size; i++) {
            elementData[i] = operator.apply((E) elementData[i]); //调用替代方法
        }  
        if (modCount != expectedModCount) { //检查并发修改
            throw new ConcurrentModificationException();
        }
        modCount++;
    }
       
```

##### 1.2.5.9.2 sort



```java
    @Override
    @SuppressWarnings("unchecked")
    public void sort(Comparator<? super E> c) {
        final int expectedModCount = modCount; // 并发修改标记位
        Arrays.sort((E[]) elementData, 0, size, c);  //数组排序
        if (modCount != expectedModCount) {
            throw new ConcurrentModificationException();
        }
        modCount++;
    }
```







到此为止,ArrayList源码就看完了, 其实也不是很复杂.



### 1.2.4 LinkedList

#### 1.2.4.1 preliminary

##### 1.2.4.1.1 Queue接口

```latex
A collection designed for holding elements prior to processing. Besides basic Collection operations, queues provide additional insertion, extraction, and inspection operations. Each of these methods exists in two forms: one throws an exception if the operation fails, the other returns a special value (either null or false, depending on the operation). The latter form of the insert operation is designed specifically for use with capacity-restricted Queue implementations; in most implementations, insert operations cannot fail. 
    一个被设计为从前往后持有元素的集合. 除了基础的集合操作, 队列提供额外的插入、提取和检查操作. 每种方法存在两种形式: 一种是执行失败抛出异常,一种则是返回特定的值(可能为null也可能为false, 取决于操作). 后者插入的形式被设计为针对使用特定策略的队列实现. 在大多数的实现中, insert操作不能够失败. 
Summary of Queue methods
			Throws exception    Returns special value    
    Insert   add(e)              offer(e)
    Remove   remove()            poll()
    Examine  element()           peek()
Queues typically, but do not necessarily, order elements in a FIFO (first-in-first-out) manner. Among the exceptions are priority queues, which order elements according to a supplied comparator, or the elements' natural ordering, and LIFO queues (or stacks) which order the elements LIFO (last-in-first-out). 
Queue典型的是按照FIFO排序元素(但也不是必须). 除此之外则是优先级队列, 其根据提供的比较器,或者元素本身的特征排序元素, 以及LIFO Queue(也称为Stack) 其按照先进后出排序元素
Whatever the ordering used, the head of the queue is that element which would be removed by a call to remove() or poll(). In a FIFO queue, all new elements are inserted at the tail of the queue. Other kinds of queues may use different placement rules. Every Queue implementation must specify its ordering properties.
无论使用哪种排序, queue头部的元素将会因为调用remove() 或者 poll()而被移除. 在FIFO queue中, 所有的元素被插入到queue的尾部. 其他类型的queue则会使用不同的替代规则. 每一个queue的实现必须确定其排序特征. 
The offer method inserts an element if possible, otherwise returning false. This differs from the Collection.add method, which can fail to add an element only by throwing an unchecked exception. The offer method is designed for use when failure is a normal, rather than exceptional occurrence, for example, in fixed-capacity (or "bounded") queues.
offer方法尽可能的插入元素, 否则的话返回false. 这与集合的策略不同. add方法可以添加元素失败(只通过抛出一个unchecked exception来达到目的).  offer方法被设计用于失败情况是常见的, 而不是偶然性的异常, 几个例子, 如在受约束的(有边界)的queue中.
The remove() and poll() methods remove and return the head of the queue. Exactly which element is removed from the queue is a function of the queue's ordering policy, which differs from implementation to implementation. The remove() and poll() methods differ only in their behavior when the queue is empty: the remove() method throws an exception, while the poll() method returns null.
 remove方法和poll方法移除并且返回queue的头部. 具体那个元素被移除取决于queue的偶爱徐策略, 各种实现的策略都不尽相同. remove方法和poll方法仅仅当queue是空的的时候才展现出行为的不同. remove方法抛出异常, 与此同时poll返回空. 
The element() and peek() methods return, but do not remove, the head of the queue.
element和peek方法返回队头元素,但是并不移除.
The Queue interface does not define the blocking queue methods, which are common in concurrent programming. These methods, which wait for elements to appear or for space to become available, are defined in the java.util.concurrent.BlockingQueue interface, which extends this interface.
Queue implementations generally do not allow insertion of null elements, although some implementations, such as LinkedList, do not prohibit insertion of null. Even in the implementations that permit it, null should not be inserted into a Queue, as null is also used as a special return value by the poll method to indicate that the queue contains no elements.
queue接口没有定义阻塞队列的方法(这类方法在并发编程中非常的常见). 这类方法(在java.util.concurrent.BlockingQueue接口中被定义,其继承自Queue接口)等待元素的出现或者可用的插入空间. Queue的实现通常不允许插入空元素, 虽然一些实现, 比如说LinkedList没有阻止null的插入. 即使在允许插入null的实现中, null最好不要被插入到queue中, 因为null也特别用于poll方法特定的返回用于指代queue不包含有任何元素.
Queue implementations generally do not define element-based versions of methods equals and hashCode but instead inherit the identity based versions from class Object, because element-based equality is not always well-defined for queues with the same elements but different ordering properties.
This interface is a member of the Java Collections Framework.
queue的实现通常不会定义"基于元素"版本的equals和hashCode方法而是从Object类中继承"基于身份"的版本, 因为对于queue来说"基于元素"的相等对于有着相同的元素却有着不同优先级的,但是不总是能被很好的定义 
本接口时java集合框架中的一个成员.


```

```java
public interface Queue<E> extends Collection<E> {

    boolean add(E e);

  
    boolean offer(E e);
    
   
    E remove();

    
    E poll();

  
    E element();

  
    E peek();
}
```

Queue接口继承自Collection, 



##### 1.2.4.1.2 DeQueue接口

```latex
A linear collection that supports element insertion and removal at both ends. The name deque is short for "double ended queue" and is usually pronounced "deck". Most Deque implementations place no fixed limits on the number of elements they may contain, but this interface supports capacity-restricted deques as well as those with no fixed size limit.
一个支持同时在头部和尾部插入元素的集合. deque 是一个短名"double ended queue" 并且常常发音为"deck". 大多数的Deque的实现对元素的容量没有限制, 但是本接口既支持有界队列也支持无界队列. 
This interface defines methods to access the elements at both ends of the deque. Methods are provided to insert, remove, and examine the element. Each of these methods exists in two forms: one throws an exception if the operation fails, the other returns a special value (either null or false, depending on the operation). The latter form of the insert operation is designed specifically for use with capacity-restricted Deque implementations; in most implementations, insert operations cannot fail.
The twelve methods described above are summarized in the following table:
本接口定义可以访问队头和队尾的元素的方法. 这些方法提供了插入,删除,以及检查元素的功能. 每种方法存在两种形式: 如果操作失败一种抛出异常, 另一种则是返回特定的值(无论是null还是false,取决于操作). 后者插入的形式被设计为针对使用特定策略的队列实现. 在大多数的实现中, insert操作不能够失败. 


```

![image-20230905204511177](https://woldier-pic-repo-1309997478.cos.ap-chengdu.myqcloud.com/woldier/2023%2F09%2Fc93eccb130f53685409271fe72234406.png)

> This interface extends the Queue interface. When a deque is used as a queue, FIFO (First-In-First-Out) behavior results. Elements are added at the end of the deque and removed from the beginning. The methods inherited from the Queue interface are precisely equivalent to Deque methods as indicated in the following table:
>
> 本接口继承自Queue接口. 当一个Deque被当作一个queue使用时, 那么展现出的是FIFO行为. 队头的元素被移除,新添加的元素则会添加到队尾. 本接口从Queue中继承的方法其实现与Queue应该具有相同的行为. 

![image-20230905212524169](https://woldier-pic-repo-1309997478.cos.ap-chengdu.myqcloud.com/woldier/2023%2F09%2F26cb24e3a7c4a987bccae80f2a43eec9.png)

> Deques can also be used as LIFO (Last-In-First-Out) stacks. This interface should be used in preference to the legacy Stack class. When a deque is used as a stack, elements are pushed and popped from the beginning of the deque. Stack methods are precisely equivalent to Deque methods as indicated in the table below:
>
> Deque可以表现为LIFO(也叫做stack 栈). 本接口应该应该被用于表现出策略的Stack. 当一个deque被当作stack使用, 元素的插入与弹出都在队列的头部.  Stack中的方法与Deque中的方法的等价关系如下:



![image-20230905212545389](https://woldier-pic-repo-1309997478.cos.ap-chengdu.myqcloud.com/woldier/2023%2F09%2Fdf1b5225459f736b5fb2c36f184e1d23.png)

> This interface provides two methods to remove interior elements, removeFirstOccurrence and removeLastOccurrence.
>
> 本接口提供两个方法用于删除元素, `removeFirstOccurrence` 和`removeLastOccurrence`.
>
> Unlike the List interface, this interface does not provide support for indexed access to elements.
>
> 与List接口不同, 本接口并不支持随机访问元素.
>
> While Deque implementations are not strictly required to prohibit the insertion of null elements, they are strongly encouraged to do so. Users of any Deque implementations that do allow null elements are strongly encouraged not to take advantage of the ability to insert nulls. This is so because null is used as a special return value by various methods to indicated that the deque is empty.
>
> 虽然Deque实现并没有规定需要阻止null元素的插入, 但是强烈建议这样做. 我们强烈建议任何允许空元素的 Deque 实现的用户不要利用插入空元素的功能。这是因为各种方法都将 null 用作特殊的返回值，以表示 Deque 为空。
>
> Deque implementations generally do not define element-based versions of the equals and hashCode methods, but instead inherit the identity-based versions from class Object.
> This interface is a member of the Java Collections Framework.

先来看看Deque的整体结构

![image-20230906082114902](https://woldier-pic-repo-1309997478.cos.ap-chengdu.myqcloud.com/woldier/2023%2F09%2Fb60deb7c4b08a164caac32c58af2fb40.png)

可以看到其重声明了Queue中的所有方法,除此之外其重声明了Collection中的一些方法. 



最后便是Deque扩展的方法了.

```java
public interface Deque<E> extends Queue<E> {

        void addFirst(E e); //队头添加
        void addLast(E e); //队尾添加
        boolean offerFirst(E e); //队头添加(阻塞)
        boolean offerLast(E e);//队尾添加(阻塞)
        E removeFirst(); //移除队头元素,抛异常
        E removeLast(); //移除队尾元素,抛异常
        E pollFirst();//移除队头元素(阻塞)
        E pollLast(); //移除队尾(阻塞)
        E pollFirst();
        E pollLast();
        E getFirst();
        E getLast();
        E peekFirst();
        E peekLast();
        void push(E e);
        E pop();
```



还有的方法我看了下它的注释

```latex
     /**
     * Removes the first occurrence of the specified element from this deque.
     * If the deque does not contain the element, it is unchanged.
     * 移除当前第一个特定的元素, 如果Deque不包含对应的元素, 那么不会发送改变. 
     * More formally, removes the first element {@code e} such that
     * <tt>(o==null&nbsp;?&nbsp;e==null&nbsp;:&nbsp;o.equals(e))</tt>
     * (if such an element exists).
     * 更一般的, 移除第一个e元素(满足 o==null?e==null:o.equals(e))
     * Returns {@code true} if this deque contained the specified element
     * (or equivalently, if this deque changed as a result of the call).
     * 返回true如果本deque确实含有特定的元素.
     * @param o element to be removed from this deque, if present
     * @return {@code true} if an element was removed as a result of this call
     * @throws ClassCastException if the class of the specified element
     *         is incompatible with this deque
     * (<a href="Collection.html#optional-restrictions">optional</a>)
     * @throws NullPointerException if the specified element is null and this
     *         deque does not permit null elements
     * (<a href="Collection.html#optional-restrictions">optional</a>)
     */
    boolean removeFirstOccurrence(Object o);
    
    /** 本方法其定义与上文差不多,不过是移除队尾,不再赘述
     * Removes the last occurrence of the specified element from this deque.
     * If the deque does not contain the element, it is unchanged.
     * More formally, removes the last element {@code e} such that
     * <tt>(o==null&nbsp;?&nbsp;e==null&nbsp;:&nbsp;o.equals(e))</tt>
     * (if such an element exists).
     * Returns {@code true} if this deque contained the specified element
     * (or equivalently, if this deque changed as a result of the call).
     *
     * @param o element to be removed from this deque, if present
     * @return {@code true} if an element was removed as a result of this call
     * @throws ClassCastException if the class of the specified element
     *         is incompatible with this deque
     * (<a href="Collection.html#optional-restrictions">optional</a>)
     * @throws NullPointerException if the specified element is null and this
     *         deque does not permit null elements
     * (<a href="Collection.html#optional-restrictions">optional</a>)
     */
    boolean removeLastOccurrence(Object o);
```



```java
    /**
     * Pushes an element onto the stack represented by this deque (in other
     * words, at the head of this deque) if it is possible to do so
     * immediately without violating capacity restrictions, throwing an
     * {@code IllegalStateException} if no space is currently available.
     * 将一个元素放入到将此deque作为stack的集合中(换句话说, 放入队头), 如果说这个操作可以立即完成而没有违反
     * 容量的限制, 如果没有可用的容量那么则抛出IllegalStateException
     * <p>This method is equivalent to {@link #addFirst}.
     * 本方法等同于addFirst
     * @param e the element to push
     * @throws IllegalStateException if the element cannot be added at this
     *         time due to capacity restrictions
     * @throws ClassCastException if the class of the specified element
     *         prevents it from being added to this deque
     * @throws NullPointerException if the specified element is null and this
     *         deque does not permit null elements
     * @throws IllegalArgumentException if some property of the specified
     *         element prevents it from being added to this deque
     */
    void push(E e);

    /**
     * Pops an element from the stack represented by this deque.  In other
     * words, removes and returns the first element of this deque.
     * 弹出栈顶的元素, 换句话说, 移除并且返回队头的元素
     * <p>This method is equivalent to {@link #removeFirst()}.
     * 本方法与removeFirst()方法等同
     * @return the element at the front of this deque (which is the top
     *         of the stack represented by this deque)
     * @throws NoSuchElementException if this deque is empty
     */
    E pop();
```

##### 1.2.4.1.3 AbstractSequentialList

> This class provides a skeletal implementation of the List interface to minimize the effort required to implement this interface backed by a "sequential access" data store (such as a linked list). For random access data (such as an array), AbstractList should be used in preference to this class.
>
> 本类提供了一个List接口的骨架实现,以最小化实现一个序列化顺序化(或者说是链表化)的数据访问与存储实现所需要做出的努力. 如果考虑随机存取, 那么应该先考虑AbstractList而不是本类. 
>
> This class is the opposite of the AbstractList class in the sense that it implements the "random access" methods (get(int index), set(int index, E element), add(int index, E element) and remove(int index)) on top of the list's list iterator, instead of the other way around.
>
> 本类站在Abstract类中方法 (get(int index), set(int index, E element), add(int index, E element) and remove(int index)) 随机访问元素期望的对立面, 而是通过list迭代器完成对相应位置元素的索引. 
>
> To implement a list the programmer needs only to extend this class and provide implementations for the listIterator and size methods. For an unmodifiable list, the programmer need only implement the list iterator's hasNext, next, hasPrevious, previous and index methods.
>
> 为了实现本抽象类,编程者只需要继承本类并且提供listIterator和size方法的实现. 对于一个不能更改的list, 编程者只需要实现迭代器中的hasNext, next, hasPrevious, previous and index方法.
>
> For a modifiable list the programmer should additionally implement the list iterator's set method. For a variable-size list the programmer should additionally implement the list iterator's remove and add methods.
> The programmer should generally provide a void (no argument) and collection constructor, as per the recommendation in the Collection interface specification.
>
> 对于一个可被修改的list实现,编程者需要额外的实现list迭代器中的set方法. 对于大小可变的list,需要额外实现remove和add方法. 
>
> 编程者通常需要提供一个无参的构造函数,以及一个参数的构造函数.



![image-20230906110843761](https://woldier-pic-repo-1309997478.cos.ap-chengdu.myqcloud.com/woldier/2023%2F09%2Fbb3bfc67718d4cc269a9cc0b78467c8b.png)

```java
   public E get(int index) {  //获取对应位置的元素
        try {
            return listIterator(index).next(); //初始化一个ListIterator ,然后调用next得到当前索引位置的元素
        } catch (NoSuchElementException exc) {
            throw new IndexOutOfBoundsException("Index: "+index);
        }
    }

    public E set(int index, E element) {  //设置对应位置的元素
        try {
            ListIterator<E> e = listIterator(index);  //初始化itr
            E oldVal = e.next(); //得到当前索引位置的元素(即旧的值)
            e.set(element); //设置对应索引的元素
            return oldVal; //返回旧的值
        } catch (NoSuchElementException exc) {
            throw new IndexOutOfBoundsException("Index: "+index);
        }
    }

    public void add(int index, E element) { //添加元素
        try {
            listIterator(index).add(element); //初始化一个listItr到对应索引然后执行add操作
        } catch (NoSuchElementException exc) {
            throw new IndexOutOfBoundsException("Index: "+index);
        }
    }

 	public E remove(int index) {  //删除
        try {
            ListIterator<E> e = listIterator(index);  //初始化
            E outCast = e.next();
            e.remove();
            return outCast;
        } catch (NoSuchElementException exc) {
            throw new IndexOutOfBoundsException("Index: "+index);
        }
    }
    public boolean addAll(int index, Collection<? extends E> c) {
        try {
            boolean modified = false;
            ListIterator<E> e1 = listIterator(index);
            Iterator<? extends E> e2 = c.iterator();
            while (e2.hasNext()) {
                e1.add(e2.next());
                modified = true;
            }
            return modified;
        } catch (NoSuchElementException exc) {
            throw new IndexOutOfBoundsException("Index: "+index);
        }
    }

    public Iterator<E> iterator() {
        return listIterator();
    }


    public abstract ListIterator<E> listIterator(int index);
```

对于listIterator方法 在AbstractList中是已经实现了的, 但是本抽象类中再次将其定义为了抽象方法.

#### 1.2.4.2 LinkedList

> Doubly-linked list implementation of the List and Deque interfaces. Implements all optional list operations, and permits all elements (including null).
>
> List与Deque的双向链表实现. 实现了所有可选的list操作以及运行所有元素(允许为null)
>
> All of the operations perform as could be expected for a doubly-linked list. Operations that index into the list will traverse the list from the beginning or the end, whichever is closer to the specified index.
>
> 对于双链表来说，所有操作的执行情况都与预期一致。索引到列表的操作将从列表开始或结束处开始遍历列表，以更接近指定索引的位置为准。
>
> Note that this implementation is not synchronized. If multiple threads access a linked list concurrently, and at least one of the threads modifies the list structurally, it must be synchronized externally. (A structural modification is any operation that adds or deletes one or more elements; merely setting the value of an element is not a structural modification.) This is typically accomplished by synchronizing on some object that naturally encapsulates the list. If no such object exists, the list should be "wrapped" using the Collections.synchronizedList method. This is best done at creation time, to prevent accidental unsynchronized access to the list:
>     List list = Collections.synchronizedList(new LinkedList(...));
>
> 注意到本实现时非线程安全的. 如果多线程并发的访问本链表, 并且超过一个的线程修改list的结构, 那么就必须在外部保持同步. 这通常是通过包裹本list的类来完成同步的. 如果没有这样的类, 那么本list应该调用` Collections.synchronizedList method` 来被包裹. 这个过程必须在创建的时候完成, 以防止偶然的未同步的对list的访问.
>
> The iterators returned by this class's iterator and listIterator methods are fail-fast: if the list is structurally modified at any time after the iterator is created, in any way except through the Iterator's own remove or add methods, the iterator will throw a ConcurrentModificationException. Thus, in the face of concurrent modification, the iterator fails quickly and cleanly, rather than risking arbitrary, non-deterministic behavior at an undetermined time in the future.
>
> 本类的迭代器和List迭代器是 遵循`fail-fast`策略: 在创建了迭代器之后, 如果list在任意时间进行了修正(除了是调用迭代器自身的修改方法)将会抛出`ConcurrentModificationException`, 因此, 在面对并发修改时, 迭代器快速-清洁的失败, 而不是冒着随机的, 未定义的行为在任意未来的时间.
>
> Note that the fail-fast behavior of an iterator cannot be guaranteed as it is, generally speaking, impossible to make any hard guarantees in the presence of unsynchronized concurrent modification. Fail-fast iterators throw ConcurrentModificationException on a best-effort basis. Therefore, it would be wrong to write a program that depended on this exception for its correctness: the fail-fast behavior of iterators should be used only to detect bugs.
>
> 请注意，无法保证迭代器的快失效行为，因为一般来说，在非同步并发修改的情况下，不可能做出任何硬性保证。快失效迭代器会尽力抛出 ConcurrentModificationException。因此，如果程序的正确性依赖于该异常，那将是错误的：迭代器的快失效行为只能用于检测错误。

看一下自己拓展的3个属性

```java
    transient int size = 0;

    /**
     * Pointer to first node.
     * Invariant: (first == null && last == null) ||
     *            (first.prev == null && first.item != null)
     */
    transient Node<E> first;
    /**
     * Pointer to last node.
     * Invariant: (first == null && last == null) ||
     *            (last.next == null && last.item != null)
     */
    transient Node<E> last;

```

#### 1.2.4.2 Node内部类



```java
    private static class Node<E> {
        E item;
        Node<E> next; 
        Node<E> prev;

        Node(Node<E> prev, E element, Node<E> next) {
            this.item = element;
            this.next = next;
            this.prev = prev;
        }
    }
```

#### 1.2.4.3 ListItr 内部类

```java
    private class ListItr implements ListIterator<E> {
        private Node<E> lastReturned; //最近返回的
        private Node<E> next; //指向索引的指针
        private int nextIndex; //索引
        private int expectedModCount = modCount ; //记录的modify count

        ListItr(int index) {
            // assert isPositionIndex(index);
            next = (index == size) ? null : node(index); //调用的node方法会返回对应索引位置的node,如果索引不合法那么返回的也是null , 将其抽取出来也是出于通用性考虑,有的实现可能并不想要哨兵节点,那么这样整个ListItr类都需要重写了.
            nextIndex = index; //当前的索引
        }

        public boolean hasNext() { //是否还有下一个元素
            return nextIndex < size; 
        }

        public E next() {
            checkForComodification(); //检查并发修改
            if (!hasNext()) //检查是否还有下一个元素
                throw new NoSuchElementException();

            lastReturned = next; //记录最近返回的
            next = next.next; //得到下一个
            nextIndex++; //索引加一
            return lastReturned.item;
        }

        public boolean hasPrevious() {
            return nextIndex > 0;
        }

        public E previous() {
            checkForComodification(); //检查并发修改
            if (!hasPrevious()) //检查是否有前驱
                throw new NoSuchElementException();

            lastReturned = next = (next == null) ? last : next.prev; 
            nextIndex--;
            return lastReturned.item;
        }

        public int nextIndex() {
            return nextIndex;
        }

        public int previousIndex() {
            return nextIndex - 1;
        }

        public void remove() {
            checkForComodification(); //检查并发修改
            if (lastReturned == null)
                throw new IllegalStateException();

            Node<E> lastNext = lastReturned.next; //记录最近返回元素的后继
            unlink(lastReturned); //断开
            if (next == lastReturned) //如果当前的next 指针指向的是lastReturned(此情况在向前走的时候出现)
                next = lastNext; //重新设置next,如果不这样做,那么则出现了断链
            else //否则的话,说明是从前向后走的(调用的next方法)
                nextIndex--; //索引减一
            lastReturned = null; //设置lastReturned 防止多次删除
            expectedModCount++; // 保存的modifyCount加一
        }

        public void set(E e) {
            if (lastReturned == null)
                throw new IllegalStateException();
            checkForComodification();
            lastReturned.item = e;
        }

        public void add(E e) {
            checkForComodification();
            lastReturned = null;  //设置lastReturn等于null ,防止再调用remove方法
            if (next == null)  //如果索引的位置是size 那么则为null
                linkLast(e); //那么在尾部插入
            else //否则的话则是在当前元素的前面插入
                linkBefore(e, next); //把e插入当前元素之前
            nextIndex++; //索引加一
            expectedModCount++;
        }

        public void forEachRemaining(Consumer<? super E> action) {
            Objects.requireNonNull(action);
            while (modCount == expectedModCount && nextIndex < size) {
                action.accept(next.item);
                lastReturned = next;
                next = next.next;
                nextIndex++;
            }
            checkForComodification(); //最后连进行检查并发修改
        }

        final void checkForComodification() {
            if (modCount != expectedModCount)
                throw new ConcurrentModificationException();
        }
    }
```

#### 1.2.4.4 DescendingIterator内部类

````java
    private class DescendingIterator implements Iterator<E> {
        private final ListItr itr = new ListItr(size());
        public boolean hasNext() {
            return itr.hasPrevious();
        }
        public E next() {
            return itr.previous();
        }
        public void remove() {
            itr.remove();
        }
    }
````

#### 1.2.4.5 LLSpliterator

```java
    static final class LLSpliterator<E> implements Spliterator<E> {
        static final int BATCH_UNIT = 1 << 10;  // batch array size increment  一个batch 的大小
        static final int MAX_BATCH = 1 << 25;  // max batch array size; 最大的batch数目
        final LinkedList<E> list; // null OK unless traversed 可以为null 直到开始遍历
        Node<E> current;      // current node; null until initialized  当前node
        int est;              // size estimate; -1 until first needed size的估计值
        int expectedModCount; // initialized when est set  期望的modify count
        int batch;            // batch size for splits 

        LLSpliterator(LinkedList<E> list, int est, int expectedModCount) {
            this.list = list;
            this.est = est;
            this.expectedModCount = expectedModCount;
        }

        final int getEst() {
            int s; // force initialization
            final LinkedList<E> lst;
            if ((s = est) < 0) { //如果est为-1,说明没有经过初始化,那么初始化,否则if条件不成立
                if ((lst = list) == null) //判断list是否为null
                    s = est = 0; //为null 设置est为0
                else { //如果不为null
                    expectedModCount = lst.modCount; //设置modCount
                    current = lst.first;  //设置当前节点为lst的头指针
                    s = est = lst.size; //设置est size
                }
            }
            return s;
        }

        public long estimateSize() { return (long) getEst(); }

        public Spliterator<E> trySplit() { //尝试分割
            Node<E> p; //声明一个暂存指针p
            int s = getEst(); //得到size
            if (s > 1 && (p = current) != null) { //如果s大于1并且 current指针指向的不是null
                int n = batch + BATCH_UNIT; //得到
                if (n > s)
                    n = s;
                if (n > MAX_BATCH)
                    n = MAX_BATCH;
                Object[] a = new Object[n];
                int j = 0;
                do { a[j++] = p.item; } while ((p = p.next) != null && j < n);
                current = p;
                batch = j;
                est = s - j;
                return Spliterators.spliterator(a, 0, j, Spliterator.ORDERED);
            }
            return null;
        }

        public void forEachRemaining(Consumer<? super E> action) {
            Node<E> p; int n;
            if (action == null) throw new NullPointerException();
            if ((n = getEst()) > 0 && (p = current) != null) {
                current = null;
                est = 0;
                do {
                    E e = p.item;
                    p = p.next;
                    action.accept(e);
                } while (p != null && --n > 0);
            }
            if (list.modCount != expectedModCount)
                throw new ConcurrentModificationException();
        }

        public boolean tryAdvance(Consumer<? super E> action) {
            Node<E> p;
            if (action == null) throw new NullPointerException();
            if (getEst() > 0 && (p = current) != null) {
                --est;
                E e = p.item;
                current = p.next;
                action.accept(e);
                if (list.modCount != expectedModCount)
                    throw new ConcurrentModificationException();
                return true;
            }
            return false;
        }

        public int characteristics() {
            return Spliterator.ORDERED | Spliterator.SIZED | Spliterator.SUBSIZED;
        }
    }
```

#### 1.2.4.6 实现Deque的方法

##### 1.2.4.6.1 getFirst()

实现Deque接口的方法

```java
  public E getFirst() {//获取头
        final Node<E> f = first;
        if (f == null)
            throw new NoSuchElementException();
        return f.item;
    }
```



##### 1.2.4.6.2 getLast()

实现Deque接口的方法

```java
    public E getLast() { //获取尾
        final Node<E> l = last;
        if (l == null)
            throw new NoSuchElementException();
        return l.item;
    }
```

##### 1.2.4.6.3 removeFirst()

```java
  public E removeFirst() { //删除头节点
        final Node<E> f = first; 
        if (f == null)
            throw new NoSuchElementException();
        return unlinkFirst(f);
    }
```

##### 1.2.4.6.4 removeLast()

```java
 public E removeLast() {
        final Node<E> l = last;
        if (l == null)
            throw new NoSuchElementException();
        return unlinkLast(l);
    }

```

##### 1.2.4.6.5 addFirst(E e)

```java
  public void addFirst(E e) {
        linkFirst(e);
    }
```

##### 1.2.4.6.6 addLast(E e)

```java
    public void addLast(E e) {
        linkLast(e);
    }
```



##### 1.2.4.6.7 peek()

```java
    public E peek() {
        final Node<E> f = first;
        return (f == null) ? null : f.item;
    }
```

##### 1.2.4.6.8 element()

```java
    /**
     * Retrieves, but does not remove, the head (first element) of this list.
     *
     * @return the head of this list
     * @throws NoSuchElementException if this list is empty
     * @since 1.5
     */
    public E element() {
        return getFirst();
    }
```



##### 1.2.4.6.9 remove()

```java
  public E remove() {
        return removeFirst();
    }

```



##### 1.2.4.6.10 offer(E e)

```java
  public boolean offer(E e) {
        return add(e);
    }

```



##### 1.2.4.6.11 offerFirst(E e)

```java
    public boolean offerFirst(E e) {
        addFirst(e);
        return true;
    }
```



##### 1.2.4.6.12 offerLast(E e)

````java
    /**
     * Inserts the specified element at the end of this list.
     *
     * @param e the element to insert
     * @return {@code true} (as specified by {@link Deque#offerLast})
     * @since 1.6
     */
    public boolean offerLast(E e) {
        addLast(e);
        return true;
    }
````



##### 1.2.4.6.13 peekFirst()

```java
    /**
     * Retrieves, but does not remove, the first element of this list,
     * or returns {@code null} if this list is empty.
     *
     * @return the first element of this list, or {@code null}
     *         if this list is empty
     * @since 1.6
     */
    public E peekFirst() {
        final Node<E> f = first;
        return (f == null) ? null : f.item;
     }
```



##### 1.2.4.6.14 peekLast() 

```java
    public E peekLast() {
        final Node<E> l = last;
        return (l == null) ? null : l.item;
    }
```



##### 1.2.4.6.15 pollFirst()

```java
    public E pollFirst() {
        final Node<E> f = first;
        return (f == null) ? null : unlinkFirst(f);
    }
```



##### 1.2.4.6.16 pollLast()

```java
  public E pollLast() {
        final Node<E> l = last;
        return (l == null) ? null : unlinkLast(l);
    }
```



##### 1.2.4.6.17 push(E e)

```java
  public void push(E e) {
        addFirst(e);
    }
```



##### 1.2.4.6.18 pop()

```java
    public E pop() {
        return removeFirst();
    }
```



##### 1.2.4.6.19 removeFirstOccurrence

```java
    public boolean removeFirstOccurrence(Object o) {
        return remove(o);
    }

```



##### 1.2.4.6.20 removeLastOccurrence

```java
  public boolean removeLastOccurrence(Object o) {
        if (o == null) {
            for (Node<E> x = last; x != null; x = x.prev) {
                if (x.item == null) {
                    unlink(x);
                    return true;
                }
            }
        } else {
            for (Node<E> x = last; x != null; x = x.prev) {
                if (o.equals(x.item)) {
                    unlink(x);
                    return true;
                }
            }
        }
        return false;
    }
```

##### 1.2.4.6.21 descendingIterator

```java
    public Iterator<E> descendingIterator() {
        return new DescendingIterator();
    }
```



#### 1.2.4.7 实现的List接口的方法

##### 1.2.4.7.1 contains(Object o)

![image-20230908143304620](https://woldier-pic-repo-1309997478.cos.ap-chengdu.myqcloud.com/woldier/2023%2F09%2F77bbf5e905abbd8cf222283ac906f9c5.png)

继承关系如下,实现了List接口(List中的contain方法又是重写的Collection中的方法),Deque接口的contains方法(Deque中的contain方法又是重写的Collection中的方法)

对于AbstractCollection,其实现的思想是通过迭代器

```java
    public boolean contains(Object o) {
        Iterator<E> it = iterator();
        if (o==null) {
            while (it.hasNext())
                if (it.next()==null)
                    return true;
        } else {
            while (it.hasNext())
                if (o.equals(it.next()))
                    return true;
        }
        return false;
    }

```

再来看看LinkedList的实现

````java
   public boolean contains(Object o) {
        return indexOf(o) != -1; //indexOf方法找到对应元素的下标
    }
````

##### 1.2.4.7.2 size()

![image-20230908145319467](https://woldier-pic-repo-1309997478.cos.ap-chengdu.myqcloud.com/woldier/2023%2F09%2Fd8b889960283c88aefde6198113b8e9d.png)

```java
   public int size() {
        return size;
    }
```

##### 1.2.4.7.3 add(E e)

![image-20230908145502481](https://woldier-pic-repo-1309997478.cos.ap-chengdu.myqcloud.com/woldier/2023%2F09%2F99bdfa1f2761ca034f75e6a1f85ef32c.png)

```java
    /**
     * Appends the specified element to the end of this list.
     *
     * <p>This method is equivalent to {@link #addLast}.
     *
     * @param e element to be appended to this list
     * @return {@code true} (as specified by {@link Collection#add})
     */
    public boolean add(E e) {
        linkLast(e);
        return true;
    }

```

##### 1.2.4.7.4 remove(Object o)

![image-20230908145529533](https://woldier-pic-repo-1309997478.cos.ap-chengdu.myqcloud.com/woldier/2023%2F09%2Fafbeaddde38c09efa8a04d1d779917f7.png)

```java
    /**
     * Removes the first occurrence of the specified element from this list,
     * if it is present.  If this list does not contain the element, it is
     * unchanged.  More formally, removes the element with the lowest index
     * {@code i} such that
     * <tt>(o==null&nbsp;?&nbsp;get(i)==null&nbsp;:&nbsp;o.equals(get(i)))</tt>
     * (if such an element exists).  Returns {@code true} if this list
     * contained the specified element (or equivalently, if this list
     * changed as a result of the call).
     *
     * @param o element to be removed from this list, if present
     * @return {@code true} if this list contained the specified element
     */
    public boolean remove(Object o) {
        if (o == null) { //如果o是null
            for (Node<E> x = first; x != null; x = x.next) {
                if (x.item == null) {
                    unlink(x);
                    return true;
                }
            }
        } else { //o不为null
            for (Node<E> x = first; x != null; x = x.next) {
                if (o.equals(x.item)) {
                    unlink(x);
                    return true;
                }
            }
        }
        return false;
    }
```



##### 1.2.4.7.4 addAll(Collection<? extends E> c)

![image-20230908145610787](https://woldier-pic-repo-1309997478.cos.ap-chengdu.myqcloud.com/woldier/2023%2F09%2Fdf26c21343b497af6c11f928e523d723.png)

```java
    public boolean addAll(Collection<? extends E> c) {
        return addAll(size, c);
    }
```

##### 1.2.4.7.5 addAll(int index, Collection<? extends E> c)

![image-20230908145704193](https://woldier-pic-repo-1309997478.cos.ap-chengdu.myqcloud.com/woldier/2023%2F09%2Fa53f0d4c6877344902b9ffce95142662.png)

```java
    public boolean addAll(int index, Collection<? extends E> c) {
        checkPositionIndex(index);//检查索引是否合法

        Object[] a = c.toArray(); // 将c转为数组
        int numNew = a.length; //得到a的长度
        if (numNew == 0) //如果数组的长度是0, 那么就没有进行add,此时返回false 表示没有修改
            return false;
        Node<E> pred, succ; //定义一个pred指针和一个succ指针
        if (index == size) { //如果说index即插入的位置与原来的LinkedList大小一致.
            succ = null; //后继为null
            pred = last; //前驱为last指针指向的node
        } else {
            succ = node(index); //后继为index索引所指向的元素
            pred = succ.prev; //前驱为succ.prev
        }

        for (Object o : a) { //遍历
            @SuppressWarnings("unchecked") E e = (E) o; //转类型
            Node<E> newNode = new Node<>(pred, e, null); //链接节点
            if (pred == null) //如果说pred是null ,那么说明当前插入的节点是head位置,那么需要重写
                first = newNode; //first指针指向新的节点
            else
                pred.next = newNode;  // 重新设置pred的next照顾这边
            pred = newNode; //设置pred指针指向新插入的node
        }

        if (succ == null) { //如果后继为null 说明是尾部插入, 重新设置last指针
            last = pred;
        } else {
            pred.next = succ;
            succ.prev = pred;
        }

        size += numNew; //size增加
        modCount++; //modify count 增加
        return true;
    }
```



##### 1.2.4.7.6 clear()

![image-20230908160809316](https://woldier-pic-repo-1309997478.cos.ap-chengdu.myqcloud.com/woldier/2023%2F09%2F9f7f3855b534bb5059ea08ea92b0161b.png)

```java
    /**
     * Removes all of the elements from this list.
     * The list will be empty after this call returns.
     */
    public void clear() {
        // Clearing all of the links between nodes is "unnecessary", but:
        // - helps a generational GC if the discarded nodes inhabit
        //   more than one generation
        // - is sure to free memory even if there is a reachable Iterator
        for (Node<E> x = first; x != null; ) {
            Node<E> next = x.next;
            x.item = null;
            x.next = null;
            x.prev = null;
            x = next;
        }
        first = last = null;
        size = 0;
        modCount++;
    }
```

##### 1.2.4.7.7 get(int index)

```java
   public E get(int index) {
        checkElementIndex(index);
        return node(index).item;
    }
```



##### 1.2.4.7.8 set

```java
   public E set(int index, E element) {
        checkElementIndex(index);
        Node<E> x = node(index);
        E oldVal = x.item;
        x.item = element;
        return oldVal;
    }
```



##### 1.2.4.7.9 add(int index, E element)

```java
  public void add(int index, E element) {
        checkPositionIndex(index);

        if (index == size)
            linkLast(element);
        else
            linkBefore(element, node(index));
    }
```

##### 1.2.4.7.10 remove(int index)

```java

   public E remove(int index) {
        checkElementIndex(index);
        return unlink(node(index));
    }

```

##### 1.2.4.7.11 indexOf(Object o)

```java

    public int indexOf(Object o) {
        int index = 0;
        if (o == null) { //判断是否是null
            for (Node<E> x = first; x != null; x = x.next) {
                if (x.item == null)
                    return index;
                index++;
            }
        } else {
            for (Node<E> x = first; x != null; x = x.next) {
                if (o.equals(x.item))
                    return index;
                index++;
            }
        }
        return -1;
    }
```

##### 1.2.4.7.12 lastIndexOf(Object o)

```java
    public int lastIndexOf(Object o) {
        int index = size;
        if (o == null) {
            for (Node<E> x = last; x != null; x = x.prev) {
                index--;
                if (x.item == null)
                    return index;
            }
        } else {
            for (Node<E> x = last; x != null; x = x.prev) {
                index--;
                if (o.equals(x.item))
                    return index;
            }
        }
        return -1;
    }
```



##### 1.2.4.7.13 listIterator(int index)

```java
    public ListIterator<E> listIterator(int index) {
        checkPositionIndex(index);
        return new ListItr(index);
    }
```

##### 1.2.4.7.14 toArray()

```java
    public Object[] toArray() {
        Object[] result = new Object[size];
        int i = 0;
        for (Node<E> x = first; x != null; x = x.next)
            result[i++] = x.item;
        return result;
    }
```

##### 1.2.4.7.15 toArray(T[] a)

```java
    @SuppressWarnings("unchecked")
    public <T> T[] toArray(T[] a) {
        if (a.length < size)
            a = (T[])java.lang.reflect.Array.newInstance(
                                a.getClass().getComponentType(), size);
        int i = 0;
        Object[] result = a;
        for (Node<E> x = first; x != null; x = x.next)
            result[i++] = x.item;

        if (a.length > size)
            a[size] = null;

        return a;
    }
```



#### 1.2.4.8 LinkedList拓展方法

##### 1.2.4.8.1 linkFirst(E e)

```java
    private void linkFirst(E e) {
        final Node<E> f = first;
        final Node<E> newNode = new Node<>(null, e, f);
        first = newNode;
        if (f == null)
            last = newNode;
        else
            f.prev = newNode;
        size++;
        modCount++;
    }
```

##### 1.2.4.8.2 linkLast(E e)

```java
    void linkLast(E e) {
        final Node<E> l = last; //尾部指针
        final Node<E> newNode = new Node<>(l, e, null); //创建一个新的节点,其前驱为l,后继为null
        last = newNode; //尾指针指向新节点
        if (l == null) //如果说l==null
            first = newNode; //设置头指针指向newNode
        else //如果说l不为空,说明当前有元素
            l.next = newNode; //设置l的后继
        size++; //size增加
        modCount++; //modify次数增加
    }





```



##### 1.2.4.8.3  linkBefore(E e, Node succ)

```java
    /**
     * Inserts element e before non-null Node succ.
     */
    void linkBefore(E e, Node<E> succ) { // e: 待插入元素 ; succ: 后继
        // assert succ != null;
        final Node<E> pred = succ.prev; //得到前驱节点
        final Node<E> newNode = new Node<>(pred, e, succ); //创建一个新的元素,其前驱是pred,其后继是succ
        succ.prev = newNode; //succ的前驱设置为newNode
        if (pred == null) //如果pred为null
            first = newNode; //那么说明succ为原来的头节点,现在需要重新赋值头指针为newNode
        else
            pred.next = newNode; //设置pred的后继
        size++; 
        modCount++;
    }
```

##### 1.2.4.8.4 unlinkFirst(Node<E> f)

```java
    /**
     * Unlinks non-null first node f.
     */
    private E unlinkFirst(Node<E> f) {
        // assert f == first && f != null;
        final E element = f.item; //得到first指针指向的node中保存的元素
        final Node<E> next = f.next; // 得到f的next节点
        f.item = null; // 释放引用
        f.next = null; // help GC
        first = next; //first指针重新指向next
        if (next == null) //判断next是否是null,是null 说明remove后list为空
            last = null; //设置last指针指向null
        else 
            next.prev = null; //设置next.prev为null
        size--;
        modCount++;
        return element;
    }
```

##### 1.2.4.8.5 unlinkLast(Node<E> l)

 ```java
     /**
      * Unlinks non-null last node l.
      */
     private E unlinkLast(Node<E> l) {
         // assert l == last && l != null;
         final E element = l.item; //得到last指针指向node中保存的元素
         final Node<E> prev = l.prev; //得到l的前驱
         l.item = null; 
         l.prev = null; // help GC
         last = prev; 
         if (prev == null) //判断prev是否是空
             first = null;
         else
             prev.next = null; //断开
         size--;
         modCount++;
         return element;
     }
 ```

##### 1.2.4.8.6 unlink(Node<E> x)

```java
    /**
     * Unlinks non-null node x.
     */
    E unlink(Node<E> x) {
        // assert x != null;
        final E element = x.item; //得到x保存的元素
        final Node<E> next = x.next; //得到x的后继
        final Node<E> prev = x.prev; //得到x的前驱

        if (prev == null) { //如果前驱为null,那么说明x是头节点
            first = next;  // 头指针指向next
        } else { //如果前驱不为空,说明x不是头节点,那么将其断开
            prev.next = next;//断开
            x.prev = null; //help GC
        }

        if (next == null) { //如果next为null 说明x为尾部
            last = prev; //重新设置尾指针
        } else { //如果x不是尾部 ,那么设置next的前驱指针
            next.prev = prev; //断开x
            x.next = null; //help GC
        }

        x.item = null; //取消对x中item的引用
        size--; //size减小
        modCount++; //modify次数增加
        return element;
    }
```

##### 1.2.4.8.7 isElementIndex(int index)

```java
    private boolean isElementIndex(int index) {
        return index >= 0 && index < size;
    }
```

##### 1.2.4.8.8 isPositionIndex(int index)

```java
    /**
     * Tells if the argument is the index of a valid position for an
     * iterator or an add operation.
     */
    private boolean isPositionIndex(int index) {
        return index >= 0 && index <= size;
    
```



##### 1.2.4.8.9 outOfBoundsMsg(int index)

```java
   private String outOfBoundsMsg(int index) {
        return "Index: "+index+", Size: "+size;
    }
```

##### 1.2.4.8.10 checkElementIndex

```java
    private void checkElementIndex(int index) {
        if (!isElementIndex(index))
            throw new IndexOutOfBoundsException(outOfBoundsMsg(index));
    }
```



##### 1.2.4.8.11 checkPositionIndex

```java
    private void checkPositionIndex(int index) {
        if (!isPositionIndex(index))
            throw new IndexOutOfBoundsException(outOfBoundsMsg(index));
    }
```



##### 1.2.4.8.12 node




```java
    Node<E> node(int index) {
        // assert isElementIndex(index);

        if (index < (size >> 1)) { //如果index 小于list大小的一半 ,那么则从头指针向后去找
            Node<E> x = first;
            for (int i = 0; i < index; i++)
                x = x.next;
            return x;
        } else { //否则的话,从尾部向前找
            Node<E> x = last;
            for (int i = size - 1; i > index; i--)
                x = x.prev;
            return x;
        }
    }


```

1.2.4.8.13 clone

```java
    @SuppressWarnings("unchecked")
    private LinkedList<E> superClone() {
        try {
            return (LinkedList<E>) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new InternalError(e);
        }
    }

    /**
     * Returns a shallow copy of this {@code LinkedList}. (The elements
     * themselves are not cloned.)
     *
     * @return a shallow copy of this {@code LinkedList} instance
     */
    public Object clone() {
        LinkedList<E> clone = superClone();

        // Put clone into "virgin" state
        clone.first = clone.last = null;
        clone.size = 0;
        clone.modCount = 0;

        // Initialize clone with our elements
        for (Node<E> x = first; x != null; x = x.next)
            clone.add(x.item);

        return clone;
    }
```





到这里关于Collection的List部分就结束了.



## 1.3 Set

### 1.3.1 Set

>
>
>A collection that contains no duplicate elements. More formally, sets contain no pair of elements e1 and e2 such that e1.equals(e2), and at most one null element. As implied by its name, this interface models the mathematical set abstraction.
>
>一个不包含有重复元素的集合. 更通俗的讲, set 不包含又一对元素e1,e2, 其满足e1.equals(e2), 并且最多存在一个null元素. 就像接口名一样, 本接口提供了数学上 set 的抽象
>
>The Set interface places additional stipulations, beyond those inherited from the Collection interface, on the contracts of all constructors and on the contracts of the add, equals and hashCode methods. Declarations for other inherited methods are also included here for convenience. (The specifications accompanying these declarations have been tailored to the Set interface, but they do not contain any additional stipulations.)
>The additional stipulation on constructors is, not surprisingly, that all constructors must create a set that contains no duplicate elements (as defined above).
>
>Set接口展现出了额外的特性, 相较于从Collection接口继承的方法, Set对所有构造函数,equals,和hashcode方法做了额外的规定. 为了方便本接口中重新声明其他从Coleection中继承到的方法. (这些声明所附带的规范是根据Set接口定制的，但不包含任何额外的规定.) . 额外的对构造方法的约束是所有的构造函数都创建一个不包含重复元素的set. 
>
>Note: Great care must be exercised if mutable objects are used as set elements. The behavior of a set is not specified if the value of an object is changed in a manner that affects equals comparisons while the object is an element in the set. A special case of this prohibition is that it is not permissible for a set to contain itself as an element.
>
>Note: 如果将可变对象用作Set元素，必须格外小心. 当一个Object对象是Set中的一个元素,如果其value发生了变化,并且该变化会影响到equals方法, 那么Set的行为将是未定义的. 一个特殊的情况是不被允许的,就是一个set不能将自己作为元素.
>
>Some set implementations have restrictions on the elements that they may contain. For example, some implementations prohibit null elements, and some have restrictions on the types of their elements. Attempting to add an ineligible element throws an unchecked exception, typically NullPointerException or ClassCastException. Attempting to query the presence of an ineligible element may throw an exception, or it may simply return false; some implementations will exhibit the former behavior and some will exhibit the latter. More generally, attempting an operation on an ineligible element whose completion would not result in the insertion of an ineligible element into the set may throw an exception or it may succeed, at the option of the implementation. Such exceptions are marked as "optional" in the specification for this interface.
>
>一些set实现可能对它们所拥有的元素有着限制. 举个例子, 一些实现抑制空元素的插入, 一些则对元素的tpye有要求. 尝试添加一个不合适的元素将会抛出异常, 或者简单的抛出NullPointerException or ClassCastException. 尝试查询存在的不被接受的元素将会抛出后者(ClassCastException). 更通常的, 尝试 对一个不受支持的元素进行操作该操作的完成将不会导致元素的插入, 有可能是抛出异常,有可能则是直接返回成功, 取决于Set的实现.  

![image-20230909101623212](https://woldier-pic-repo-1309997478.cos.ap-chengdu.myqcloud.com/woldier/2023%2F09%2F134e633633d854280a1a1c1f8b52e5e4.png)

Set重定义的方法如上所示, 重定义了Collection中的部分方法. 因为Collection接口中的方法完全一致,并且不像List方法一样自己扩展了一些方法(如`add(int index,E e)`),所以这里就不在过多介绍. 



### 1.3.2 AbstractSet

> This class provides a skeletal implementation of the Set interface to minimize the effort required to implement this interface.
>
> 本类提供了实现Set接口的骨架,来最小化实现Set接口所需要做出的努力.
>
> The process of implementing a set by extending this class is identical to that of implementing a Collection by extending AbstractCollection, except that all of the methods and constructors in subclasses of this class must obey the additional constraints imposed by the Set interface (for instance, the add method must not permit addition of multiple instances of an object to a set).
>
> 在实现Set接口时继承AbstractSet类的过程 与 实现Collection接口时继承AbstractCollection基本相同, 除了所有的方法以及构造方法必须遵循有关set的特性(举个例子, add方法必须保证不允许额外的相同的多实例插入到set中)
>
> Note that this class does not override any of the implementations from the AbstractCollection class. It merely adds implementations for equals and hashCode.
>
> 需要注意的时,本class并没有重写AbstractCollection中的方法. 而仅仅是添加了equals和hashcode的的实现. 





```java
public abstract class AbstractSet<E> extends AbstractCollection<E> implements Set<E> {
    /**
     * Sole constructor.  (For invocation by subclass constructors, typically
     * implicit.)
     */
    protected AbstractSet() {
    }
    // Comparison and hashing
    public boolean equals(Object o) {
        if (o == this) //如果o就是自己
            return true;
        if (!(o instanceof Set)) //如果不是set
            return false;
        Collection<?> c = (Collection<?>) o; //转collection
        if (c.size() != size()) //先比较大小,大小不一样肯定也不相等
            return false;
        try {
            return containsAll(c); // 调用方法返回
        } catch (ClassCastException unused)   {
            return false;
        } catch (NullPointerException unused) {
            return false;
        }
    }
    public int hashCode() { //hashcode
        int h = 0;
        Iterator<E> i = iterator();
        while (i.hasNext()) {
            E obj = i.next();
            if (obj != null)
                h += obj.hashCode();
        }
        return h;
    }

  
    public boolean removeAll(Collection<?> c) { 
        Objects.requireNonNull(c);
        boolean modified = false;

        if (size() > c.size()) { //如果本set的长度大于c
            for (Iterator<?> i = c.iterator(); i.hasNext(); )
                modified |= remove(i.next());
        } else { //如果set的长度小于c
            for (Iterator<?> i = iterator(); i.hasNext(); ) {
                if (c.contains(i.next())) {
                    i.remove();
                    modified = true;
                }
            }
        }
        return modified;
    }

}
```

### 1.3.3 HashSet 

> This class implements the Set interface, backed by a hash table (actually a HashMap instance). It makes no guarantees as to the iteration order of the set; in particular, it does not guarantee that the order will remain constant over time. This class permits the null element.
> This class offers constant time performance for the basic operations (add, remove, contains and size), assuming the hash function disperses the elements properly among the buckets. Iterating over this set requires time proportional to the sum of the HashSet instance's size (the number of elements) plus the "capacity" of the backing HashMap instance (the number of buckets). Thus, it's very important not to set the initial capacity too high (or the load factor too low) if iteration performance is important.
> Note that this implementation is not synchronized. If multiple threads access a hash set concurrently, and at least one of the threads modifies the set, it must be synchronized externally. This is typically accomplished by synchronizing on some object that naturally encapsulates the set. If no such object exists, the set should be "wrapped" using the Collections.synchronizedSet method. This is best done at creation time, to prevent accidental unsynchronized access to the set:
>     Set s = Collections.synchronizedSet(new HashSet(...));
> The iterators returned by this class's iterator method are fail-fast: if the set is modified at any time after the iterator is created, in any way except through the iterator's own remove method, the Iterator throws a ConcurrentModificationException. Thus, in the face of concurrent modification, the iterator fails quickly and cleanly, rather than risking arbitrary, non-deterministic behavior at an undetermined time in the future.
> Note that the fail-fast behavior of an iterator cannot be guaranteed as it is, generally speaking, impossible to make any hard guarantees in the presence of unsynchronized concurrent modification. Fail-fast iterators throw ConcurrentModificationException on a best-effort basis. Therefore, it would be wrong to write a program that depended on this exception for its correctness: the fail-fast behavior of iterators should be used only to detect bugs.
> This class is a member of the Java Collections Framework.

// TODO 由于基于hashmap实现,因此 我们阅读完hashmap源码后再回来看.



### 1.3.4 TreeSet

> A NavigableSet implementation based on a TreeMap. The elements are ordered using their natural ordering, or by a Comparator provided at set creation time, depending on which constructor is used.
> This implementation provides guaranteed log(n) time cost for the basic operations (add, remove and contains).
> Note that the ordering maintained by a set (whether or not an explicit comparator is provided) must be consistent with equals if it is to correctly implement the Set interface. (See Comparable or Comparator for a precise definition of consistent with equals.) This is so because the Set interface is defined in terms of the equals operation, but a TreeSet instance performs all element comparisons using its compareTo (or compare) method, so two elements that are deemed equal by this method are, from the standpoint of the set, equal. The behavior of a set is well-defined even if its ordering is inconsistent with equals; it just fails to obey the general contract of the Set interface.
> Note that this implementation is not synchronized. If multiple threads access a tree set concurrently, and at least one of the threads modifies the set, it must be synchronized externally. This is typically accomplished by synchronizing on some object that naturally encapsulates the set. If no such object exists, the set should be "wrapped" using the Collections.synchronizedSortedSet method. This is best done at creation time, to prevent accidental unsynchronized access to the set:
>     SortedSet s = Collections.synchronizedSortedSet(new TreeSet(...));
> The iterators returned by this class's iterator method are fail-fast: if the set is modified at any time after the iterator is created, in any way except through the iterator's own remove method, the iterator will throw a ConcurrentModificationException. Thus, in the face of concurrent modification, the iterator fails quickly and cleanly, rather than risking arbitrary, non-deterministic behavior at an undetermined time in the future.
> Note that the fail-fast behavior of an iterator cannot be guaranteed as it is, generally speaking, impossible to make any hard guarantees in the presence of unsynchronized concurrent modification. Fail-fast iterators throw ConcurrentModificationException on a best-effort basis. Therefore, it would be wrong to write a program that depended on this exception for its correctness: the fail-fast behavior of iterators should be used only to detect bugs.
> This class is a member of the Java Collections Framework.

// TODO 由于基于Treemap实现,因此 我们阅读完hashmap源码后再回来看.







# 2.Map



> An object that maps keys to values. A map cannot contain duplicate keys; each key can map to at most one value.
>
> 一个对象,其将key映射成对应的value. 一个map不能包含重复的key. 一个key不能映射超过一个value.
>
> This interface takes the place of the Dictionary class, which was a totally abstract class rather than an interface.
>
> 本接口替代了Dictionary类(Dictionary是一个抽象类而不是接口)
>
> The Map interface provides three collection views, which allow a map's contents to be viewed as a set of keys, collection of values, or set of key-value mappings. The order of a map is defined as the order in which the iterators on the map's collection views return their elements. Some map implementations, like the TreeMap class, make specific guarantees as to their order; others, like the HashMap class, do not.
>
> Map接口提供了三种集合视图, 这使得map能够被是为三种形态一个key的集合,一个value的集合, 或者一个key-value对的集合映射.  Map的顺序定义为迭代器对象返回元素的顺序. 一些Map的实现, 比如TreeMap, 对元素的顺序做出了特定的要求, 其他的,比如所HashMap则没有.
>
> Note: great care must be exercised if mutable objects are used as map keys. The behavior of a map is not specified if the value of an object is changed in a manner that affects equals comparisons while the object is a key in the map. A special case of this prohibition is that it is not permissible for a map to contain itself as a key. While it is permissible for a map to contain itself as a value, extreme caution is advised: the equals and hashCode methods are no longer well defined on such a map.
>
> Note: 如果将可变对象用作Map元素，必须格外小心. 当一个Object对象是Map中的一个元素,如果其value发生了变化,并且该变化会影响到equals方法, 那么Set的行为将是未定义的. 一个特殊的情况是不被允许的,就是一个Map不能将自己作为元素.
>
> All general-purpose map implementation classes should provide two "standard" constructors: a void (no arguments) constructor which creates an empty map, and a constructor with a single argument of type Map, which creates a new map with the same key-value mappings as its argument. In effect, the latter constructor allows the user to copy any map, producing an equivalent map of the desired class. There is no way to enforce this recommendation (as interfaces cannot contain constructors) but all of the general-purpose map implementations in the JDK comply.
>
> 通常一个Map的实现需要提供两个"标准的"构造方法: 一个无参构造器,其产生一个空的map. 以及一个有一个参数的构造器其创建一个新的有着相同key-value映射的map.实际上，后一种构造函数允许用户复制任何映射，生成所需类的等价映射。我们无法强制执行这一建议（因为接口不能包含构造函数），但 JDK 中的所有通用映射实现都符合这一建议。
>
> The "destructive" methods contained in this interface, that is, the methods that modify the map on which they operate, are specified to throw UnsupportedOperationException if this map does not support the operation. If this is the case, these methods may, but are not required to, throw an UnsupportedOperationException if the invocation would have no effect on the map. For example, invoking the putAll(Map) method on an unmodifiable map may, but is not required to, throw the exception if the map whose mappings are to be "superimposed" is empty.
>
> 该接口中包含的 "破坏性 "方法，即修改所操作的地图的方法，如果该地图不支持该操作，则指定抛出 UnsupportedOperationException。在这种情况下，如果调用对映射没有影响，这些方法可以（但不是必须）抛出 UnsupportedOperationException。例如，在不可修改的映射上调用 putAll(Map) 方法时，如果映射将被 "叠加 "的映射是空的，则可能（但不是必须）抛出异常。
>
> Some map implementations have restrictions on the keys and values they may contain. For example, some implementations prohibit null keys and values, and some have restrictions on the types of their keys. Attempting to insert an ineligible key or value throws an unchecked exception, typically NullPointerException or ClassCastException. Attempting to query the presence of an ineligible key or value may throw an exception, or it may simply return false; some implementations will exhibit the former behavior and some will exhibit the latter. More generally, attempting an operation on an ineligible key or value whose completion would not result in the insertion of an ineligible element into the map may throw an exception or it may succeed, at the option of the implementation. Such exceptions are marked as "optional" in the specification for this interface.
>
> 一些 Map 接口的实现对它们可以包含的键和值有一些限制。例如，一些实现禁止使用 null 键和值，而一些对键的类型有限制。尝试插入不合格的键或值会引发一个未经检查的异常，通常是 NullPointerException 或 ClassCastException。尝试查询不合格的键或值的存在可能会引发异常，或者可能只会返回 false；一些实现将展现前一种行为，而一些将展现后一种行为。更一般地说，对不合格的键或值进行操作，如果其完成不会导致将不合格的元素插入到 Map 中，可能会引发异常，也可能会成功，这取决于实现的选项。此类异常在该接口的规范中被标记为“可选”。
>
> Many methods in Collections Framework interfaces are defined in terms of the equals method. For example, the specification for the containsKey(Object key) method says: "returns true if and only if this map contains a mapping for a key k such that `(key==null ? k==null : key.equals(k))`." This specification should not be construed to imply that invoking Map.containsKey with a non-null argument key will cause key.equals(k) to be invoked for any key k. Implementations are free to implement optimizations whereby the equals invocation is avoided, for example, by first comparing the hash codes of the two keys. (The Object.hashCode() specification guarantees that two objects with unequal hash codes cannot be equal.) More generally, implementations of the various Collections Framework interfaces are free to take advantage of the specified behavior of underlying Object methods wherever the implementor deems it appropriate.
>
> Collections Framework 接口中的许多方法是根据 equals 方法定义的。例如，containsKey(Object key) 方法的规范表示：“仅当此映射包含一个键 k 的映射，使得 `(key==null ? k==null : key.equals(k))`” 时返回 true。这个规范不应被解释为调用 Map.containsKey 时，对于非空参数 key 会导致 key.equals(k) 被调用以用于任何键 k。实现可以自由地实现优化，避免 equals 调用，例如，首先比较两个键的哈希码。（Object.hashCode() 规范保证具有不相等哈希码的两个对象不能相等。）更一般地说，各种 Collections Framework 接口的实现可以在实现者认为适当的地方利用底层 Object 方法的指定行为。
>
> Some map operations which perform recursive traversal of the map may fail with an exception for self-referential instances where the map directly or indirectly contains itself. This includes the clone(), equals(), hashCode() and toString() methods. Implementations may optionally handle the self-referential scenario, however most current implementations do not do so.
>
> 在执行对Map进行递归遍历的某些地图操作时，如果Map直接或间接包含自身，可能会导致异常。这包括 clone()、equals()、hashCode() 和 toString() 方法。实现可以选择性地处理这种自引用情况，但目前大多数实现不这样做。

![image-20230909163509232](https://woldier-pic-repo-1309997478.cos.ap-chengdu.myqcloud.com/woldier/2023%2F09%2F5ca95aed26a66012a9195296647e96d3.png)



## 2.1 Entry内部接口

在Java中，`Map`接口的`Entry`内部接口（实际上是一个嵌套接口）用于表示`Map`中的键值对（key-value pair）。它是`Map`的一个成员内部接口，用于封装`Map`中的数据，包括键和与之关联的值。

`Entry`接口通常包含两个方法：

1. `getKey()`：这个方法用于获取与`Entry`关联的键。

2. `getValue()`：这个方法用于获取与`Entry`关联的值。

通过`Map`的`entrySet()`方法，可以获得包含所有`Entry`对象的集合，然后可以迭代这些`Entry`对象，从中获取键和值。

以下是一个示例，演示如何使用`Entry`接口来遍历`Map`中的键值对：

```java
import java.util.*;

public class MapEntryExample {
    public static void main(String[] args) {
        Map<String, Integer> map = new HashMap<>();
        map.put("Alice", 25);
        map.put("Bob", 30);
        map.put("Charlie", 22);

        // 获取Map中的Entry集合
        Set<Map.Entry<String, Integer>> entrySet = map.entrySet();

        // 遍历Entry集合并输出键值对
        for (Map.Entry<String, Integer> entry : entrySet) {
            String key = entry.getKey();
            Integer value = entry.getValue();
            System.out.println("Key: " + key + ", Value: " + value);
        }
    }
}
```

通过使用`Entry`接口，您可以方便地访问`Map`中的键和值，以便进行各种操作，如遍历、更新或删除键值对。这有助于更灵活地操作`Map`的内容。



```java
    /**
     * A map entry (key-value pair).  The <tt>Map.entrySet</tt> method returns
     * a collection-view of the map, whose elements are of this class.  The
     * <i>only</i> way to obtain a reference to a map entry is from the
     * iterator of this collection-view.  These <tt>Map.Entry</tt> objects are
     * valid <i>only</i> for the duration of the iteration; more formally,
     * the behavior of a map entry is undefined if the backing map has been
     * modified after the entry was returned by the iterator, except through
     * the <tt>setValue</tt> operation on the map entry.
     * map entity（键-值对）。Map.entrySet方法返回地图的集合视图，其中的元素属于这个类。
     * 获取地图条目的map entity是通过这个集合视图的迭代器。这些Map.Entry对象仅在迭代期间有效；
     * 更正式地说，如果在迭代器返回条目后修改了底层地图，地图条目的行为将变得不确定，
     * 除非通过地图条目上的setValue操作。
     * @see Map#entrySet()
     * @since 1.2
     */
    interface Entry<K,V> {
        /**
         * Returns the key corresponding to this entry.
         * 返回entity的key
         * @return the key corresponding to this entry
         * @throws IllegalStateException implementations may, but are not
         *         required to, throw this exception if the entry has been
         *         removed from the backing map.
         */
        K getKey();

        /**
         * Returns the value corresponding to this entry.  If the mapping
         * has been removed from the backing map (by the iterator's
         * <tt>remove</tt> operation), the results of this call are undefined.
         * 返回entity的value. 如果映射关系已经被后端的map移除(通过迭代器的remove方法), 
         * 那么本方法的返回时未定义的. 
         * @return the value corresponding to this entry
         * @throws IllegalStateException implementations may, but are not
         *         required to, throw this exception if the entry has been
         *         removed from the backing map.
         */
        V getValue();

        /**
         * Replaces the value corresponding to this entry with the specified
         * value (optional operation).  (Writes through to the map.)  The
         * behavior of this call is undefined if the mapping has already been
         * removed from the map (by the iterator's <tt>remove</tt> operation).
         * 用给的的value代替相应entry中的value(可选操作). 
         * @param value new value to be stored in this entry
         * @return old value corresponding to the entry
         * @throws UnsupportedOperationException if the <tt>put</tt> operation
         *         is not supported by the backing map
         * @throws ClassCastException if the class of the specified value
         *         prevents it from being stored in the backing map
         * @throws NullPointerException if the backing map does not permit
         *         null values, and the specified value is null
         * @throws IllegalArgumentException if some property of this value
         *         prevents it from being stored in the backing map
         * @throws IllegalStateException implementations may, but are not
         *         required to, throw this exception if the entry has been
         *         removed from the backing map.
         */
        V setValue(V value);

        /**
         * Compares the specified object with this entry for equality.
         * Returns <tt>true</tt> if the given object is also a map entry and
         * the two entries represent the same mapping.  More formally, two
         * entries <tt>e1</tt> and <tt>e2</tt> represent the same mapping
         * if<pre>
         *     (e1.getKey()==null ?
         *      e2.getKey()==null : e1.getKey().equals(e2.getKey()))  &amp;&amp;
         *     (e1.getValue()==null ?
         *      e2.getValue()==null : e1.getValue().equals(e2.getValue()))
         * </pre>
         * This ensures that the <tt>equals</tt> method works properly across
         * different implementations of the <tt>Map.Entry</tt> interface.
         *
         * @param o object to be compared for equality with this map entry
         * @return <tt>true</tt> if the specified object is equal to this map
         *         entry
         */
        boolean equals(Object o);

        /**
         * Returns the hash code value for this map entry.  The hash code
         * of a map entry <tt>e</tt> is defined to be: <pre>
         *     (e.getKey()==null   ? 0 : e.getKey().hashCode()) ^
         *     (e.getValue()==null ? 0 : e.getValue().hashCode())
         * </pre>
         * This ensures that <tt>e1.equals(e2)</tt> implies that
         * <tt>e1.hashCode()==e2.hashCode()</tt> for any two Entries
         * <tt>e1</tt> and <tt>e2</tt>, as required by the general
         * contract of <tt>Object.hashCode</tt>.
         *
         * @return the hash code value for this map entry
         * @see Object#hashCode()
         * @see Object#equals(Object)
         * @see #equals(Object)
         */
        int hashCode();

        /**
         * Returns a comparator that compares {@link Map.Entry} in natural order on key.
         *
         * <p>The returned comparator is serializable and throws {@link
         * NullPointerException} when comparing an entry with a null key.
         *
         * @param  <K> the {@link Comparable} type of then map keys
         * @param  <V> the type of the map values
         * @return a comparator that compares {@link Map.Entry} in natural order on key.
         * @see Comparable
         * @since 1.8
         */
        public static <K extends Comparable<? super K>, V> Comparator<Map.Entry<K,V>> comparingByKey() {
            return (Comparator<Map.Entry<K, V>> & Serializable)
                (c1, c2) -> c1.getKey().compareTo(c2.getKey());
        }

        /**
         * Returns a comparator that compares {@link Map.Entry} in natural order on value.
         *
         * <p>The returned comparator is serializable and throws {@link
         * NullPointerException} when comparing an entry with null values.
         *
         * @param <K> the type of the map keys
         * @param <V> the {@link Comparable} type of the map values
         * @return a comparator that compares {@link Map.Entry} in natural order on value.
         * @see Comparable
         * @since 1.8
         */
        public static <K, V extends Comparable<? super V>> Comparator<Map.Entry<K,V>> comparingByValue() {
            return (Comparator<Map.Entry<K, V>> & Serializable)
                (c1, c2) -> c1.getValue().compareTo(c2.getValue());
        }

        /**
         * Returns a comparator that compares {@link Map.Entry} by key using the given
         * {@link Comparator}.
         * 传入一个比较器进行比较
         * <p>The returned comparator is serializable if the specified comparator
         * is also serializable.
         *
         * @param  <K> the type of the map keys
         * @param  <V> the type of the map values
         * @param  cmp the key {@link Comparator}
         * @return a comparator that compares {@link Map.Entry} by the key.
         * @since 1.8
         */
        public static <K, V> Comparator<Map.Entry<K, V>> comparingByKey(Comparator<? super K> cmp) {
            Objects.requireNonNull(cmp);
            return (Comparator<Map.Entry<K, V>> & Serializable)
                (c1, c2) -> cmp.compare(c1.getKey(), c2.getKey());
        }

        /**
         * Returns a comparator that compares {@link Map.Entry} by value using the given
         * {@link Comparator}.
         *
         * <p>The returned comparator is serializable if the specified comparator
         * is also serializable.
         *
         * @param  <K> the type of the map keys
         * @param  <V> the type of the map values
         * @param  cmp the value {@link Comparator}
         * @return a comparator that compares {@link Map.Entry} by the value.
         * @since 1.8
         */
        public static <K, V> Comparator<Map.Entry<K, V>> comparingByValue(Comparator<? super V> cmp) {
            Objects.requireNonNull(cmp);
            return (Comparator<Map.Entry<K, V>> & Serializable)
                (c1, c2) -> cmp.compare(c1.getValue(), c2.getValue());
        }
    }
```



## 2.2 Map的方法

定义的抽象方法

```java
    int size(); //返回当前映射的key-value对的数目
    boolean isEmpty(); //是否为空
    boolean containsKey(Object key);  //是否包含某个key
    boolean containsValue(Object value);  //是否包含某个value
    V get(Object key); //根据key返回对应的value
    V put(K key, V value); //设置key对应的value值,如果说存在旧的value值,那么旧的value值将会被替代
    V remove(Object key);  //根据key移除, 如果含有的对应key-value 那么返回value ,否则返回null ,如果允许null为null ,那么null并不能表明map中不存在对应的key
    void putAll(Map<? extends K, ? extends V> m);
    void clear();
    Set<K> keySet(); //返回key的集合
    Collection<V> values(); //返回value的集合
    Set<Map.Entry<K, V>> entrySet(); //返回键值对的集合

```



重定义Object的方法

Object类中有具体实现,但是这里将其重新定义为了抽象方法

```java
    boolean equals(Object o);
    int hashCode();
```



默认方法

```java
    /**
     * Returns the value to which the specified key is mapped, or
     * {@code defaultValue} if this map contains no mapping for the key.
     * 返回key映射的值, 或者如果map中不包含该key的映射则返回给定的默认值
     * @implSpec
     * The default implementation makes no guarantees about synchronization
     * or atomicity properties of this method. Any implementation providing
     * atomicity guarantees must override this method and document its
     * concurrency properties.
     * 默认的实现并没有对并发做出任何的确保, 或者原子操作. 任何提供原子操作的实现类必须提供相应的文档注释.
     * @param key the key whose associated value is to be returned 想要根据key取得value
     * @param defaultValue the default mapping of the key  对key默认的映射
     * @return the value to which the specified key is mapped, or 
     * {@code defaultValue} if this map contains no mapping for the key
     * @throws ClassCastException if the key is of an inappropriate type for
     * this map
     * (<a href="{@docRoot}/java/util/Collection.html#optional-restrictions">optional</a>)
     * @throws NullPointerException if the specified key is null and this map
     * does not permit null keys
     * (<a href="{@docRoot}/java/util/Collection.html#optional-restrictions">optional</a>)
     * @since 1.8
     */
    default V getOrDefault(Object key, V defaultValue) {
        V v;
        return (((v = get(key)) != null) || containsKey(key))
            ? v
            : defaultValue;
    }

    /**
     * Performs the given action for each entry in this map until all entries
     * have been processed or the action throws an exception.   Unless
     * otherwise specified by the implementing class, actions are performed in
     * the order of entry set iteration (if an iteration order is specified.)
     * Exceptions thrown by the action are relayed to the caller.
     * 对所有entry执行给定的操作, 直到所有的entry都被执行过相应的action 或者再执行acton中出现异常则停止.
     * 除非被实现类特定遍历顺序, 否则的话将按照entry set的迭代顺序对元素执行相应的action
     * @implSpec
     * The default implementation is equivalent to, for this {@code map}:
     * <pre> {@code
     * for (Map.Entry<K, V> entry : map.entrySet())
     *     action.accept(entry.getKey(), entry.getValue());
     * }</pre>
     *
     * The default implementation makes no guarantees about synchronization
     * or atomicity properties of this method. Any implementation providing
     * atomicity guarantees must override this method and document its
     * concurrency properties.
     * 
     * @param action The action to be performed for each entry
     * @throws NullPointerException if the specified action is null 
     * @throws ConcurrentModificationException if an entry is found to be
     * removed during iteration
     * @since 1.8
     */
    default void forEach(BiConsumer<? super K, ? super V> action) {
        Objects.requireNonNull(action);
        for (Map.Entry<K, V> entry : entrySet()) {
            K k;
            V v;
            try {
                k = entry.getKey();
                v = entry.getValue();
            } catch(IllegalStateException ise) {
                // this usually means the entry is no longer in the map.
                throw new ConcurrentModificationException(ise);
            }
            action.accept(k, v);
        }
    }

    /**
     * Replaces each entry's value with the result of invoking the given
     * function on that entry until all entries have been processed or the
     * function throws an exception.  Exceptions thrown by the function are
     * relayed to the caller.
     * 通过调用给定的function返回的值替代所有entry的value直到所有entry被遍历,或者时抛出了异常. function抛出的异常依赖于调用者
     * @implSpec
     * <p>The default implementation is equivalent to, for this {@code map}:
     * <pre> {@code
     * for (Map.Entry<K, V> entry : map.entrySet())
     *     entry.setValue(function.apply(entry.getKey(), entry.getValue()));
     * }</pre>
     *
     * <p>The default implementation makes no guarantees about synchronization
     * or atomicity properties of this method. Any implementation providing
     * atomicity guarantees must override this method and document its
     * concurrency properties.
     *
     * @param function the function to apply to each entry
     * @throws UnsupportedOperationException if the {@code set} operation
     * is not supported by this map's entry set iterator.
     * @throws ClassCastException if the class of a replacement value
     * prevents it from being stored in this map
     * @throws NullPointerException if the specified function is null, or the
     * specified replacement value is null, and this map does not permit null
     * values
     * @throws ClassCastException if a replacement value is of an inappropriate
     *         type for this map
     *         (<a href="{@docRoot}/java/util/Collection.html#optional-restrictions">optional</a>)
     * @throws NullPointerException if function or a replacement value is null,
     *         and this map does not permit null keys or values
     *         (<a href="{@docRoot}/java/util/Collection.html#optional-restrictions">optional</a>)
     * @throws IllegalArgumentException if some property of a replacement value
     *         prevents it from being stored in this map
     *         (<a href="{@docRoot}/java/util/Collection.html#optional-restrictions">optional</a>)
     * @throws ConcurrentModificationException if an entry is found to be
     * removed during iteration
     * @since 1.8
     */
    default void replaceAll(BiFunction<? super K, ? super V, ? extends V> function) {
        Objects.requireNonNull(function);
        for (Map.Entry<K, V> entry : entrySet()) {
            K k;
            V v;
            try {
                k = entry.getKey();
                v = entry.getValue();
            } catch(IllegalStateException ise) {
                // this usually means the entry is no longer in the map.
                throw new ConcurrentModificationException(ise);
            }

            // ise thrown from function is not a cme.
            v = function.apply(k, v);

            try {
                entry.setValue(v);
            } catch(IllegalStateException ise) {
                // this usually means the entry is no longer in the map.
                throw new ConcurrentModificationException(ise);
            }
        }
    }


    /**
     * If the specified key is not already associated with a value (or is mapped
     * to {@code null}) associates it with the given value and returns
     * {@code null}, else returns the current value.
     * 如果特定的key并没有与一个value关联起来(或者mapping的是null值) 将key关联给定的value并且返回null, 否则返回当前的value
     * @implSpec
     * The default implementation is equivalent to, for this {@code
     * map}:
     * 
     * <pre> {@code
     * V v = map.get(key);
     * if (v == null)
     *     v = map.put(key, value);
     *
     * return v;
     * }</pre>
     *
     * <p>The default implementation makes no guarantees about synchronization
     * or atomicity properties of this method. Any implementation providing
     * atomicity guarantees must override this method and document its
     * concurrency properties.
     *
     * @param key key with which the specified value is to be associated
     * @param value value to be associated with the specified key
     * @return the previous value associated with the specified key, or
     *         {@code null} if there was no mapping for the key.
     *         (A {@code null} return can also indicate that the map
     *         previously associated {@code null} with the key,
     *         if the implementation supports null values.)
     * @throws UnsupportedOperationException if the {@code put} operation
     *         is not supported by this map
     *         (<a href="{@docRoot}/java/util/Collection.html#optional-restrictions">optional</a>)
     * @throws ClassCastException if the key or value is of an inappropriate
     *         type for this map
     *         (<a href="{@docRoot}/java/util/Collection.html#optional-restrictions">optional</a>)
     * @throws NullPointerException if the specified key or value is null,
     *         and this map does not permit null keys or values
     *         (<a href="{@docRoot}/java/util/Collection.html#optional-restrictions">optional</a>)
     * @throws IllegalArgumentException if some property of the specified key
     *         or value prevents it from being stored in this map
     *         (<a href="{@docRoot}/java/util/Collection.html#optional-restrictions">optional</a>)
     * @since 1.8
     */
    default V putIfAbsent(K key, V value) {
        V v = get(key);
        if (v == null) {
            v = put(key, value);
        }

        return v;
    }

    /**
     * Removes the entry for the specified key only if it is currently
     * mapped to the specified value.
     * 移除一个entry ,当且仅当特定的key关联的value与给出了value是一致的
     * @implSpec
     * The default implementation is equivalent to, for this {@code map}:
     *
     * <pre> {@code
     * if (map.containsKey(key) && Objects.equals(map.get(key), value)) {
     *     map.remove(key);
     *     return true;
     * } else
     *     return false;
     * }</pre>
     *
     * <p>The default implementation makes no guarantees about synchronization
     * or atomicity properties of this method. Any implementation providing
     * atomicity guarantees must override this method and document its
     * concurrency properties.
     *
     * @param key key with which the specified value is associated
     * @param value value expected to be associated with the specified key
     * @return {@code true} if the value was removed
     * @throws UnsupportedOperationException if the {@code remove} operation
     *         is not supported by this map
     *         (<a href="{@docRoot}/java/util/Collection.html#optional-restrictions">optional</a>)
     * @throws ClassCastException if the key or value is of an inappropriate
     *         type for this map
     *         (<a href="{@docRoot}/java/util/Collection.html#optional-restrictions">optional</a>)
     * @throws NullPointerException if the specified key or value is null,
     *         and this map does not permit null keys or values
     *         (<a href="{@docRoot}/java/util/Collection.html#optional-restrictions">optional</a>)
     * @since 1.8
     */
    default boolean remove(Object key, Object value) {
        Object curValue = get(key); //获取当前的value
        if (!Objects.equals(curValue, value) ||
            (curValue == null && !containsKey(key))) {
            return false;
        }
        remove(key);
        return true;
    }


    /**
     * Replaces the entry for the specified key only if currently
     * mapped to the specified value.
     * 替代给定key对应的entry, 当且仅当entry的value与oldValue一致时
     * @implSpec
     * The default implementation is equivalent to, for this {@code map}:
     *
     * <pre> {@code
     * if (map.containsKey(key) && Objects.equals(map.get(key), value)) {
     *     map.put(key, newValue);
     *     return true;
     * } else
     *     return false;
     * }</pre>
     *
     * The default implementation does not throw NullPointerException
     * for maps that do not support null values if oldValue is null unless
     * newValue is also null.
     *
     * <p>The default implementation makes no guarantees about synchronization
     * or atomicity properties of this method. Any implementation providing
     * atomicity guarantees must override this method and document its
     * concurrency properties.
     *
     * @param key key with which the specified value is associated
     * @param oldValue value expected to be associated with the specified key
     * @param newValue value to be associated with the specified key
     * @return {@code true} if the value was replaced
     * @throws UnsupportedOperationException if the {@code put} operation
     *         is not supported by this map
     *         (<a href="{@docRoot}/java/util/Collection.html#optional-restrictions">optional</a>)
     * @throws ClassCastException if the class of a specified key or value
     *         prevents it from being stored in this map
     * @throws NullPointerException if a specified key or newValue is null,
     *         and this map does not permit null keys or values
     * @throws NullPointerException if oldValue is null and this map does not
     *         permit null values
     *         (<a href="{@docRoot}/java/util/Collection.html#optional-restrictions">optional</a>)
     * @throws IllegalArgumentException if some property of a specified key
     *         or value prevents it from being stored in this map
     * @since 1.8
     */
    default boolean replace(K key, V oldValue, V newValue) {
        Object curValue = get(key);
        if (!Objects.equals(curValue, oldValue) ||
            (curValue == null && !containsKey(key))) {
            return false;
        }
        put(key, newValue);
        return true;
    }

    /**
     * Replaces the entry for the specified key only if it is
     * currently mapped to some value.
     * 代替key映射的value ,当且仅当映射的旧value不是null的时候
     * @implSpec
     * The default implementation is equivalent to, for this {@code map}:
     *
     * <pre> {@code
     * if (map.containsKey(key)) {
     *     return map.put(key, value);
     * } else
     *     return null;
     * }</pre>
     *
     * <p>The default implementation makes no guarantees about synchronization
     * or atomicity properties of this method. Any implementation providing
     * atomicity guarantees must override this method and document its
     * concurrency properties.
      *
     * @param key key with which the specified value is associated
     * @param value value to be associated with the specified key
     * @return the previous value associated with the specified key, or
     *         {@code null} if there was no mapping for the key.
     *         (A {@code null} return can also indicate that the map
     *         previously associated {@code null} with the key,
     *         if the implementation supports null values.)
     * @throws UnsupportedOperationException if the {@code put} operation
     *         is not supported by this map
     *         (<a href="{@docRoot}/java/util/Collection.html#optional-restrictions">optional</a>)
     * @throws ClassCastException if the class of the specified key or value
     *         prevents it from being stored in this map
     *         (<a href="{@docRoot}/java/util/Collection.html#optional-restrictions">optional</a>)
     * @throws NullPointerException if the specified key or value is null,
     *         and this map does not permit null keys or values
     * @throws IllegalArgumentException if some property of the specified key
     *         or value prevents it from being stored in this map
     * @since 1.8
     */
    default V replace(K key, V value) {
        V curValue;
        if (((curValue = get(key)) != null) || containsKey(key)) {
            curValue = put(key, value);
        }
        return curValue;
    }

    /**
     * If the specified key is not already associated with a value (or is mapped
     * to {@code null}), attempts to compute its value using the given mapping
     * function and enters it into this map unless {@code null}.
     * 如果key还没有和任何的value值绑定(或者绑定的时null), 尝试用给出的func计算其value, 并把得到的key
     -value 对加入到map中, 除非计算得到的value值为null. 
     * <p>If the function returns {@code null} no mapping is recorded. If
     * the function itself throws an (unchecked) exception, the
     * exception is rethrown, and no mapping is recorded.  The most
     * common usage is to construct a new object serving as an initial
     * mapped value or memoized result, as in:
     * 如果function返回了一个null,那么不会有映射关系被记录. 如果function自身抛出了一个未被检查的异常, 那么该异常将会再次被抛出, 也没有映射关系被记录
     * 本方法最常见的用法时构造一个新的对象被当作初始化的mapping value 
     * <pre> {@code
     * map.computeIfAbsent(key, k -> new Value(f(k)));
     * }</pre>
     *
     * <p>Or to implement a multi-value map, {@code Map<K,Collection<V>>},
     * supporting multiple values per key:
     * 或者作为实现多值map Map<K,Collection<V>>
     * <pre> {@code
     * map.computeIfAbsent(key, k -> new HashSet<V>()).add(v);
     * }</pre>
     *
     *
     * @implSpec
     * The default implementation is equivalent to the following steps for this
     * {@code map}, then returning the current value or {@code null} if now
     * absent:
     * 
     * <pre> {@code
     * if (map.get(key) == null) {
     *     V newValue = mappingFunction.apply(key);
     *     if (newValue != null)
     *         map.put(key, newValue);
     * }
     * }</pre>
     *
     * <p>The default implementation makes no guarantees about synchronization
     * or atomicity properties of this method. Any implementation providing
     * atomicity guarantees must override this method and document its
     * concurrency properties. In particular, all implementations of
     * subinterface {@link java.util.concurrent.ConcurrentMap} must document
     * whether the function is applied once atomically only if the value is not
     * present.
     *
     * @param key key with which the specified value is to be associated
     * @param mappingFunction the function to compute a value
     * @return the current (existing or computed) value associated with
     *         the specified key, or null if the computed value is null
     * @throws NullPointerException if the specified key is null and
     *         this map does not support null keys, or the mappingFunction
     *         is null
     * @throws UnsupportedOperationException if the {@code put} operation
     *         is not supported by this map
     *         (<a href="{@docRoot}/java/util/Collection.html#optional-restrictions">optional</a>)
     * @throws ClassCastException if the class of the specified key or value
     *         prevents it from being stored in this map
     *         (<a href="{@docRoot}/java/util/Collection.html#optional-restrictions">optional</a>)
     * @since 1.8
     */
    default V computeIfAbsent(K key,
            Function<? super K, ? extends V> mappingFunction) {
        Objects.requireNonNull(mappingFunction);
        V v;
        if ((v = get(key)) == null) {
            V newValue;
            if ((newValue = mappingFunction.apply(key)) != null) {
                put(key, newValue);
                return newValue;
            }
        }

        return v;
    }

    /**
     * If the value for the specified key is present and non-null, attempts to
     * compute a new mapping given the key and its current mapped value.
     * 如果当前key对应的value存在并且不为null, 那么就尝试计算一个新的映射value替换掉原有的
     * <p>If the function returns {@code null}, the mapping is removed.  If the
     * function itself throws an (unchecked) exception, the exception is
     * rethrown, and the current mapping is left unchanged.
    *
     * @implSpec
     * The default implementation is equivalent to performing the following
     * steps for this {@code map}, then returning the current value or
     * {@code null} if now absent:
     *
     * <pre> {@code
     * if (map.get(key) != null) {
     *     V oldValue = map.get(key);
     *     V newValue = remappingFunction.apply(key, oldValue);
     *     if (newValue != null)
     *         map.put(key, newValue);
     *     else
     *         map.remove(key);
     * }
     * }</pre>
     *
     * <p>The default implementation makes no guarantees about synchronization
     * or atomicity properties of this method. Any implementation providing
     * atomicity guarantees must override this method and document its
     * concurrency properties. In particular, all implementations of
     * subinterface {@link java.util.concurrent.ConcurrentMap} must document
     * whether the function is applied once atomically only if the value is not
     * present.
     *
     * @param key key with which the specified value is to be associated
     * @param remappingFunction the function to compute a value
     * @return the new value associated with the specified key, or null if none
     * @throws NullPointerException if the specified key is null and
     *         this map does not support null keys, or the
     *         remappingFunction is null
     * @throws UnsupportedOperationException if the {@code put} operation
     *         is not supported by this map
     *         (<a href="{@docRoot}/java/util/Collection.html#optional-restrictions">optional</a>)
     * @throws ClassCastException if the class of the specified key or value
     *         prevents it from being stored in this map
     *         (<a href="{@docRoot}/java/util/Collection.html#optional-restrictions">optional</a>)
     * @since 1.8
     */
    default V computeIfPresent(K key,
            BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        Objects.requireNonNull(remappingFunction);
        V oldValue;
        if ((oldValue = get(key)) != null) {
            V newValue = remappingFunction.apply(key, oldValue);
            if (newValue != null) {
                put(key, newValue);
                return newValue;
            } else {
                remove(key);
                return null;
            }
        } else {
            return null;
        }
    }

    /**
     * Attempts to compute a mapping for the specified key and its current
     * mapped value (or {@code null} if there is no current mapping). For
     * example, to either create or append a {@code String} msg to a value
     * mapping:
     * 尝试对一个特定的key计算一个映射关系value(或者是null,如果当前没有映射关系). 
     * <pre> {@code
     * map.compute(key, (k, v) -> (v == null) ? msg : v.concat(msg))}</pre>
     * (Method {@link #merge merge()} is often simpler to use for such purposes.)
     * 
     * <p>If the function returns {@code null}, the mapping is removed (or
     * remains absent if initially absent).  If the function itself throws an
     * (unchecked) exception, the exception is rethrown, and the current mapping
     * is left unchanged.
     * 如果方法返回null, 那么说明映射关系被移除了(或者是仍然缺席的如果说初始化为缺席). 如果function自己抛出了 未检查的异常, 
     * 那么该异常将会再次被抛出, 那么现有的映射关系将不会被改变. 
     * @implSpec
     * The default implementation is equivalent to performing the following
     * steps for this {@code map}, then returning the current value or
     * {@code null} if absent:
     *
     * <pre> {@code
     * V oldValue = map.get(key);
     * V newValue = remappingFunction.apply(key, oldValue);
     * if (oldValue != null ) { //判断oldValue是不为空
     *    if (newValue != null) //判断计算的newValue是否为null 
     *       map.put(key, newValue); //不为null 则对key建立新的映射关系
     *    else
     *       map.remove(key); //为null , 那么则做key的移除
     * } else { //判断oldValue是为空
     *    if (newValue != null) //不为null 则对key建立新的映射关系
     *       map.put(key, newValue);
     *    else
     *       return null; //为null , 那么不做任何的事情
     * }
     * }</pre>
     *
     * <p>The default implementation makes no guarantees about synchronization
     * or atomicity properties of this method. Any implementation providing
     * atomicity guarantees must override this method and document its
     * concurrency properties. In particular, all implementations of
     * subinterface {@link java.util.concurrent.ConcurrentMap} must document
     * whether the function is applied once atomically only if the value is not
     * present.
     *
     * @param key key with which the specified value is to be associated
     * @param remappingFunction the function to compute a value
     * @return the new value associated with the specified key, or null if none
     * @throws NullPointerException if the specified key is null and
     *         this map does not support null keys, or the
     *         remappingFunction is null
     * @throws UnsupportedOperationException if the {@code put} operation
     *         is not supported by this map
     *         (<a href="{@docRoot}/java/util/Collection.html#optional-restrictions">optional</a>)
     * @throws ClassCastException if the class of the specified key or value
     *         prevents it from being stored in this map
     *         (<a href="{@docRoot}/java/util/Collection.html#optional-restrictions">optional</a>)
     * @since 1.8
     */
    default V compute(K key,
            BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        Objects.requireNonNull(remappingFunction);
        V oldValue = get(key);

        V newValue = remappingFunction.apply(key, oldValue);
        if (newValue == null) {
            // delete mapping
            if (oldValue != null || containsKey(key)) {
                // something to remove
                remove(key);
                return null;
            } else {
                // nothing to do. Leave things as they were.
                return null;
            }
        } else {
            // add or replace old mapping
            put(key, newValue);
            return newValue;
        }
    }


    /**
     * If the specified key is not already associated with a value or is
     * associated with null, associates it with the given non-null value.
     * Otherwise, replaces the associated value with the results of the given
     * remapping function, or removes if the result is {@code null}. This
     * method may be of use when combining multiple mapped values for a key.
     * For example, to either create or append a {@code String msg} to a
     * value mapping:
     * 如果特定的key还仍未与一个value进行映射或者映射为null , 那么将其与一个非null的value进行绑定.
     * 否则,  通过给定的remapping function得到的value替代当前映射的旧value值, 或者说如果function返回的是null那么删除映射关系.
     * 
     * <pre> {@code
     * map.merge(key, msg, String::concat)
     * }</pre>
     *
     * <p>If the function returns {@code null} the mapping is removed.  If the
     * function itself throws an (unchecked) exception, the exception is
     * rethrown, and the current mapping is left unchanged.
     *
     * @implSpec
     * The default implementation is equivalent to performing the following
     * steps for this {@code map}, then returning the current value or
     * {@code null} if absent:
     *
     * <pre> {@code
     * V oldValue = map.get(key);
     * V newValue = (oldValue == null) ? value :
     *              remappingFunction.apply(oldValue, value);
     * if (newValue == null)
     *     map.remove(key);
     * else
     *     map.put(key, newValue);
     * }</pre>
     *
     * <p>The default implementation makes no guarantees about synchronization
     * or atomicity properties of this method. Any implementation providing
     * atomicity guarantees must override this method and document its
     * concurrency properties. In particular, all implementations of
     * subinterface {@link java.util.concurrent.ConcurrentMap} must document
     * whether the function is applied once atomically only if the value is not
     * present.
     *
     * @param key key with which the resulting value is to be associated
     * @param value the non-null value to be merged with the existing value
     *        associated with the key or, if no existing value or a null value
     *        is associated with the key, to be associated with the key
     * @param remappingFunction the function to recompute a value if present
     * @return the new value associated with the specified key, or null if no
     *         value is associated with the key
     * @throws UnsupportedOperationException if the {@code put} operation
     *         is not supported by this map
     *         (<a href="{@docRoot}/java/util/Collection.html#optional-restrictions">optional</a>)
     * @throws ClassCastException if the class of the specified key or value
     *         prevents it from being stored in this map
     *         (<a href="{@docRoot}/java/util/Collection.html#optional-restrictions">optional</a>)
     * @throws NullPointerException if the specified key is null and this map
     *         does not support null keys or the value or remappingFunction is
     *         null
     * @since 1.8
     */
    default V merge(K key, V value,
            BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
        Objects.requireNonNull(remappingFunction);
        Objects.requireNonNull(value);
        V oldValue = get(key);
        V newValue = (oldValue == null) ? value :
                   remappingFunction.apply(oldValue, value);
        if(newValue == null) {
            remove(key);
        } else {
            put(key, newValue);
        }
        return newValue;
    }

```



## 2.3 AbstractMap

> This class provides a skeletal implementation of the Map interface, to minimize the effort required to implement this interface.
>
> 本类提供了一个实现Map接口的骨架, 来最小化实现该接口所需要做出的努力.
>
> To implement an unmodifiable map, the programmer needs only to extend this class and provide an implementation for the entrySet method, which returns a set-view of the map's mappings. Typically, the returned set will, in turn, be implemented atop AbstractSet. This set should not support the add or remove methods, and its iterator should not support the remove method.
>
> 为了实现一个不可更改的map, 编程者只需要继承本class并且提供entrySet方法的实现, 该方法返回map映射关系的set视图.  反过来, 返回的Set是继承自AbstractSet的. 那么该set最好不要支持add 或者remove方法, 并且其迭代器不应该支持remove方法.
>
> To implement a modifiable map, the programmer must additionally override this class's put method (which otherwise throws an UnsupportedOperationException), and the iterator returned by entrySet().iterator() must additionally implement its remove method.
> The programmer should generally provide a void (no argument) and map constructor, as per the recommendation in the Map interface specification.
>
> 为了实现一个可修改的map, 编程者必须额外的重写本类的out方法(不这样做的话该方法会抛出UnsupportedOperationException异常), 并且entrySet().iterator() 迭代器也需要额外的实现remove方法. 编程者需要提供无参的构造函数, 以及有一个参数(该参数接受Map类型的特定实例)的构造函数.
>
> The documentation for each non-abstract method in this class describes its implementation in detail. Each of these methods may be overridden if the map being implemented admits a more efficient implementation
>
> 该类中每个非抽象方法的文档都详细描述了其实现方法。如果要实现的映射需要更高效的实现，则可以重写这些方法



