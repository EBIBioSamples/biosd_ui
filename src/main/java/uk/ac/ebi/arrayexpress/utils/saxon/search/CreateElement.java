package uk.ac.ebi.arrayexpress.utils.saxon.search;

import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.SimpleExpression;
import net.sf.saxon.expr.StaticProperty;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.instruct.Executable;
import net.sf.saxon.om.Item;
import net.sf.saxon.style.ExtensionInstruction;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.ObjectValue;

public class CreateElement extends ExtensionInstruction
{

    public CreateElement()
    {
    }

    public void prepareAttributes() throws XPathException
    {
    }

    public void validate() throws XPathException
    {
        super.validate();
    }

    public Expression compile(Executable exec) throws XPathException
    {
        return new CreateInstruction();
    }

    private static class CreateInstruction extends SimpleExpression
    {
        public CreateInstruction()
        {
        }

        /**
         * A subclass must provide one of the methods evaluateItem(), iterate(), or process().
         * This method indicates which of the three is provided.
         */

        public int getImplementationMethod()
        {
            return Expression.EVALUATE_METHOD;
        }

        public int computeCardinality()
        {
            return StaticProperty.EXACTLY_ONE;
        }

        public String getExpressionType()
        {
            return "lucene:create";
        }

        public Item evaluateItem(XPathContext context) throws XPathException
        {
            // do something here today
            return new ObjectValue(new Object());
        }
    }
}
