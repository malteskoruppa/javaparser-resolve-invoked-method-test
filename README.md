# javaparser-resolve-invoked-method-test

This repository contains two simple tests for JavaParser's
`MethodCallExpr#resolveInvokedMethod()` method which demonstrate two
bugs in JavaParser's symbol solver logic. Essentially, these tests
use JavaParser to parse the following two classes:

Class A:
```
package org.testapp;

public class A {
  public void foo() {}
  private void bar() {}
}
```

Class B:
```
package org.testapp;

public class B extends A {

  public void test1() {
    foo(); // should be inherited from A, so there should exist an (implicit) method org.testapp.B.foo()
  }

  public void test2() {
    bar(); // should not be resolvable, since bar() is private in A and not inherited by B
  }
}
```

Next, the nodes that correspond to the two method call expressions
`foo()` (in method `B.test1()`) and `bar()` (in method `B.test2()`)
are obtained. Then, `resolveInvokedMethod()` is called on both of
these nodes.

In the first case (`foo()`), JavaParser says that the called method is
`org.testapp.A.foo()`. However, in reality, the called method is
`org.testapp.B.foo()`, since method `foo()` is implicitly inherited by
class `B`.  (Note: `org.testapp.A.foo()` would be correct if the call
expression was `super.foo()` instead of plain `foo()`).

In the second case, JavaParser says that the called method is
`org.testapp.A.bar()`. However, in reality, the call is invalid: there
is no method `bar()` in class `B`, since `bar()` has private access in
class `A`. Therefore, an `UnsolvedSymbolException` should be thrown.

