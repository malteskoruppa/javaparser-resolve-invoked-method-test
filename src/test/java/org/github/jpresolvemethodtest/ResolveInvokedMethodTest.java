package org.github.jpresolvemethodtest;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.resolution.UnsolvedSymbolException;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileNotFoundException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ResolveInvokedMethodTest {

	/**
	 * Gets the CompilationUnit generated from org/testapp/B.java (in src/test/resources)
	 */
	private CompilationUnit getCompilationUnit() throws FileNotFoundException {

		// root path to the application to be parsed
		String rootPath = "src" + File.separator +
		                  "test" + File.separator +
		                  "resources";

		// initialize the symbol solver
		CombinedTypeSolver typeSolver = new CombinedTypeSolver();
		typeSolver.add(new ReflectionTypeSolver());
		typeSolver.add(new JavaParserTypeSolver(new File(rootPath)));
		JavaSymbolSolver symbolSolver = new JavaSymbolSolver(typeSolver);
		JavaParser.getStaticConfiguration().setSymbolResolver(symbolSolver);

		// parse the file B.java
		String classBPath = rootPath + File.separator +
		                    "org" + File.separator +
		                    "testapp" + File.separator +
		                    "B.java";
		return JavaParser.parse(new File(classBPath));
	}

	/**
	 * Test 1: resolve the method call expression foo() in the method org.testapp.B.test1()
	 */
	@Test
	public void testResolveImplicitlyInheritedMethod1() throws FileNotFoundException {

		CompilationUnit cu = getCompilationUnit();

		// get the method call expression that we want to test resolveInvokedMethod() on
		MethodCallExpr methodCallExpr = cu.getType(0) // get class B
				                                .asClassOrInterfaceDeclaration().getMember(0) // get method test1
				                                .asMethodDeclaration().getBody().get() // get method body
				                                .asBlockStmt().getStatement(0) // get the first and only statement
				                                .asExpressionStmt().getExpression() // get the method call expression
				                                .asMethodCallExpr();

		// FAILS!
		// resolveInvokedMethod().getQualifiedName() incorrectly returns org.testapp.A.foo instead of org.testapp.B.foo
		assertEquals("org.testapp.B.foo", methodCallExpr.resolveInvokedMethod().getQualifiedName());
	}

	/**
	 * Test 2: (try to) resolve the method call expression bar() in the method org.testapp.B.test2()
	 */
	@Test
	public void testResolveInaccessibleMethod() throws FileNotFoundException {

		CompilationUnit cu = getCompilationUnit();

		// get the method call expression that we want to test resolveInvokedMethod() on
		MethodCallExpr methodCallExpr = cu.getType(0) // get class B
				                                .asClassOrInterfaceDeclaration().getMember(1) // get method test2
				                                .asMethodDeclaration().getBody().get() // get method body
				                                .asBlockStmt().getStatement(0) // get the first and only statement
				                                .asExpressionStmt().getExpression() // get the method call expression
				                                .asMethodCallExpr();

		// FAILS!
		// resolveInvokedMethod() should throw an exception, but instead the parent class's private method is returned
		assertThrows(UnsolvedSymbolException.class, methodCallExpr::resolveInvokedMethod);
	}
}
