package org.umlg.sqlg;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.umlg.sqlg.test.doctests.TestForDocs;

/**
 * Date: 2014/07/16
 * Time: 12:10 PM
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
//        TestRequiredProperty.class,
//        TestRequiredPropertyDistributed.class,
//        TestDefaultValue.class,
//        TestDefaultValueDistributed.class,
//        TestPropertyCheckConstraint.class,
//        TestPropertyCheckConstraintDistributed.class,
//        TestMultiplicityOnArrayTypes.class
        TestForDocs.class
})
public class AnyTest {
}
