// $Id$

package com.jclark.xsl.sax;

import java.net.URL;
import java.lang.reflect.*;
import org.xml.sax.*;

import com.jclark.xsl.om.XSLException;
import com.jclark.xsl.om.Node;
import com.jclark.xsl.expr.ExtensionContext;
import com.jclark.xsl.tr.ExtensionHandler;
import com.jclark.xsl.tr.Result;
import com.jclark.xsl.tr.ResultFragmentVariant;
import com.jclark.xsl.tr.ResultFragmentVariantBase;

/**
 * for invoking XSLT extension functions (??)
 */
public class ExtensionHandlerImpl implements ExtensionHandler 
{
    private static class ContextImpl implements ExtensionContext 
    {
        private final Class cls;

        ContextImpl(Class cls) 
        {
            this.cls = cls;
        }

        public boolean available(String name) 
        {
            if (name.equals("new")) {
                return cls.getConstructors().length != 0;
            }
            name = camelCase(name);
            Method[] methods = cls.getMethods();
            for (int i = 0; i < methods.length; i++) {
                if (methods[i].getName().equals(name)) {
                    return true;
                }
            }
            return false;
        }

        //
        //
        private Object callMethod(String name, Object[] args)
            throws XSLException, IllegalAccessException, 
                   IllegalArgumentException, InvocationTargetException 
        {
            name = camelCase(name);
            Method[] methods = cls.getMethods();
            Method method = null;
            boolean mustBeStatic = true;
            if (args.length > 0 && cls.isInstance(args[0])) {
                mustBeStatic = false;
            }
            for (int i = 0; i < methods.length; i++) {
                if (methods[i].getName().equals(name)
                    && (Modifier.isStatic(methods[i].getModifiers())
                        ? methods[i].getParameterTypes().length == args.length
                        : (!mustBeStatic
                           && methods[i].getParameterTypes().length == args.length - 1))) {
                    if (method != null)
                        throw new XSLException(name + ": overloaded method");
                    method = methods[i];
                }
            }
            if (method == null) {
                throw new XSLException(name + ": no method with " + 
                                       args.length + " arguments");
            }
            Object result;
            if (Modifier.isStatic(method.getModifiers()))
                result = method.invoke(null, args);
            else {
                Object obj = args[0];
                Object[] newArgs = new Object[args.length - 1];
                System.arraycopy(args, 1, newArgs, 0, newArgs.length);
                result = method.invoke(obj, newArgs);
            }
            if (result instanceof ResultTreeFragment) {
                return wrapResultTreeFragment((ResultTreeFragment)result);
            }
            return result;
        }

        //
        //
        private Object callConstructor(Object[] args)
            throws XSLException, InstantiationException, 
                   IllegalAccessException, IllegalArgumentException,
                   InvocationTargetException 
        {
            Constructor[] constructors = cls.getConstructors();
            Constructor constructor = null;
            for (int i = 0; i < constructors.length; i++) {
                if (constructors[i].getParameterTypes().length == args.length) {
                    if (constructor != null)
                        throw new XSLException("overloaded constructor");
                    constructor = constructors[i];
                }
            }
            if (constructor == null)
                throw new XSLException("no constructor with " + args.length + " arguments");
            return constructor.newInstance(args);
        }

        /**
         *
         */
        public Object call(String name, Node currentNode, Object[] args)
            throws XSLException
        {
            try {
                if (name.equals("new"))
                    return callConstructor(args);
                else
                    return callMethod(name, args);
            }
            catch (IllegalAccessException e) {
                throw new XSLException(name + ": illegal access");
            }
            catch (IllegalArgumentException e) {
                throw new XSLException(name + ": illegal arguments");
            }
            catch (InstantiationException e) {
                throw new XSLException("cannot instantiate class");
            }
            catch (InvocationTargetException e) {
                Throwable t = e.getTargetException();
                if (t instanceof RuntimeException)
                    throw (RuntimeException)t;
                if (t instanceof Error)
                    throw (Error)t;
                if (t instanceof Exception)
                    throw new XSLException((Exception)t);
                throw new XSLException(e);
            }
        }
    
        private String camelCase(String name)
        {
            int i = name.indexOf('-');
            if (i < 0 || i + 1 == name.length())
                return name;
            return (name.substring(0, i)
                    + Character.toUpperCase(name.charAt(i + 1))
                    + camelCase(name.substring(i + 2)));
        }
    }

    static final private String JAVA_NS = "http://www.jclark.com/xt/java/";

    /**
     *
     */
    public ExtensionContext createContext(String namespace)
        throws XSLException
    {
        if (namespace.startsWith(JAVA_NS)) {
            try {
                return new ContextImpl(Class.forName(namespace.substring(JAVA_NS.length())));
            }
            catch (ClassNotFoundException e) { }
        }
        return null;
    }

    /**
     *
     */
    private static Object wrapResultTreeFragment(final ResultTreeFragment frag)
    {
        return new ResultFragmentVariantBase()
            {
                public Object convertToObject() {
                    return frag;
                }
                public void append(Result result) throws XSLException {
                    if (result instanceof ResultBase) {
                        ((ResultBase)result).resultTreeFragment(frag);
                    } else {
                        // FIXME
                        throw new XSLException("weird result tree fragment usage not implemented");
                    }
                }
            };
    }

    /**
     *
     */
    public Object wrapResultFragmentVariant(final ResultFragmentVariant frag)
    {
        return new ResultTreeFragment()
            {
                public void emit(DocumentHandler handler) throws SAXException
                {
                    ResultBase result = new MultiNamespaceResult(handler, null);
                    try {
                        frag.append(result);
                        result.flush();
                    }
                    catch (XSLException e) {
                        throw new SAXException(e);
                    }
                }
            };
    }
}
