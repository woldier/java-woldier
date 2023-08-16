> # java集合类

# 1.Collection



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



