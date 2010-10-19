/*
 * Copyright (c) 1999-2004 Sourceforge JACOB Project.
 * All rights reserved. Originator: Dan Adler (http://danadler.com).
 * Get more information about JACOB at http://sourceforge.net/projects/jacob-project
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */
package org.racob.com;

/**
 * Object represents MS level dispatch object. Each instance of this points at
 * some data structure on the MS windows side.
 * 
 * 
 * <p>
 * You're going to live here a lot
 */
public class Dispatch extends IUnknown {

    /**
     * Used to set the locale in a call. The user locale is another option
     */
    public static final int LOCALE_SYSTEM_DEFAULT = 2048;
    /** used by callN() and callSubN() */
    public static final int Method = 1;
    /** used by callN() and callSubN() */
    public static final int Get = 2;
    /** used by put() */
    public static final int Put = 4;
    /** not used, probably intended for putRef() */
    public static final int PutRef = 8;
    /**
     * One of legal values for GetDispId. Not used in this layer and probably
     * not needed.
     */
    public static final int fdexNameCaseSensitive = 1;
    /** program Id passed in by ActiveX components in their constructor */
    private String programId = null;

    /**
     * Dummy empty array used one doesn't have to be created on every invocation
     */
    private final static Object[] NO_OBJECT_ARGS = new Object[0];
    /**
     * Dummy empty array used one doesn't have to be created on every invocation
     */
    public final static Variant[] NO_VARIANT_ARGS = new Variant[0];
    /**
     * Dummy empty array used one doesn't have to be created on every invocation
     */
    private final static int[] NO_INT_ARGS = new int[0];

    /**
     * zero argument constructor that sets the dispatch pointer to 0 This is the
     * only way to create a Dispatch without a value in the pointer field.
     */
    public Dispatch() {
        super();
    }

    /**
     * This constructor calls createInstance with progid. This is the
     * constructor used by the ActiveXComponent or by programs that don't like
     * the activeX interface but wish to create new connections to windows
     * programs.
     * <p>
     * This constructor always creates a new windows/program object because it
     * is based on the CoCreate() windows function.
     * <p>
     *
     * @param requestedProgramId
     * @throws IllegalArgumentException
     *             if null is passed in as the program id
     *             <p>
     */
    public Dispatch(String requestedProgramId) {
        super();
        programId = requestedProgramId;
        if (programId != null && !"".equals(programId)) {
            pointer.set(createInstanceNative(requestedProgramId));
        } else {
            throw new IllegalArgumentException(
                    "Dispatch(String) does not accept null or an empty string as a parameter");
        }
    }

    /**
     * native call createInstance only used by the constructor with the same
     * parm type. This probably should be private. It is the wrapper for the
     * Windows CoCreate() call
     * <P>
     * This ends up calling CoCreate down in the JNI layer
     * <p>
     * The behavior is different if a ":" character exists in the progId. In
     * that case CoGetObject and CreateInstance (someone needs to describe this
     * better)
     *
     * @param progid
     */
    private native int createInstanceNative(String progid);

    /**
     * native call getActiveInstance only used by the constructor with the same
     * parm type. This probably should be private. It is the wrapper for the
     * Windows GetActiveObject() call
     * <P>
     * This ends up calling GetActiveObject down in the JNI layer
     * <p>
     * This does not have the special behavior for program ids with ":" in them
     * that createInstance has.
     *
     * @param progid
     */
    private native int getActiveInstanceNative(String progid);

    /**
     * Wrapper around the native method
     *
     * @param pProgramIdentifier
     *            name of the program you wish to connect to
     */
    protected void getActiveInstance(String pProgramIdentifier) {
        if (pProgramIdentifier == null || "".equals(pProgramIdentifier)) {
            throw new IllegalArgumentException("program id is required");
        }
        programId = pProgramIdentifier;
        pointer.set(getActiveInstanceNative(pProgramIdentifier));
    }

    /**
     * native call coCreateInstance only used by the constructor with the same
     * parm type. This probably should be private. It is the wrapper for the
     * Windows CoCreate() call
     * <P>
     * This ends up calling CoCreate down in the JNI layer
     * <p>
     * This does not have the special behavior for program ids with ":" in them
     * that createInstance has.
     *
     * @param progid
     */
    private native int coCreateInstanceNative(String progid);

    /**
     * Wrapper around the native method
     *
     * @param pProgramIdentifier
     */
    protected void coCreateInstance(String pProgramIdentifier) {
        if (pProgramIdentifier == null || "".equals(pProgramIdentifier)) {
            throw new IllegalArgumentException("program id is required");
        }
        this.programId = pProgramIdentifier;
        pointer.set(coCreateInstanceNative(pProgramIdentifier));
    }

    /**
     * Return a different interface by IID string.
     * <p>
     * Once you have a Dispatch object, you can navigate to the other interfaces
     * of a COM object by calling QueryInterafce. The argument is an IID string
     * in the format: "{9BF24410-B2E0-11D4-A695-00104BFF3241}". You typically
     * get this string from the idl file (it's called uuid in there). Any
     * interface you try to use must be derived from IDispatch. T The atl
     * example uses this.
     * <p>
     * The Dispatch instance resulting from this query is instanciated in the
     * JNI code.
     *
     * @param iid
     * @return Dispatch a disptach that matches ??
     */
    public Dispatch QueryInterface(String iid) {
        return QueryInterface(pointer.get(), iid);
    }

    private native Dispatch QueryInterface(int pointer, String iid);

    public TypeInfo getTypeInfo() {
        return getTypeInfo(pointer.get());
    }

    private native TypeInfo getTypeInfo(int pointer);

    /**
     * Constructor that only gets called from JNI QueryInterface calls JNI code
     * that looks up the object for the key passed in. The JNI CODE then creates
     * a new dispatch object using this constructor
     *
     * @param pDisp
     */
    public Dispatch(int pointer) {
        super(pointer);
    }

    /**
     * Constructor to be used by subclass that want to swap themselves in for
     * the default Dispatch class. Usually you will have a class like
     * WordDocument that is a subclass of Dispatch and it will have a
     * constructor public WordDocument(Dispatch). That constructor should just
     * call this constructor as super(Dispatch)
     *
     * @param dispatchToBeDisplaced
     */
    public Dispatch(Dispatch dispatchToBeDisplaced) {
        super(dispatchToBeDisplaced.pointer.get()); // TAKE OVER THE IDispatch POINTER
        dispatchToBeDisplaced.pointer.invalidate(); // NULL OUT THE INPUT POINTER
    }

    /**
     * returns the program id if an activeX component created this otherwise it
     * returns null. This was added to aid in debugging
     *
     * @return the program id an activeX component was created against
     */
    public String getProgramId() {
        return programId;
    }

    /**
     * @param theOneInQuestion
     *            dispatch being tested
     * @throws IllegalStateException
     *             if this dispatch isn't hooked up
     * @throws IllegalArgumentException
     *             if null the dispatch under test is null
     */
    private static void throwIfUnattachedDispatch(Dispatch theOneInQuestion) {
        if (theOneInQuestion == null) {
            throw new IllegalArgumentException("Can't pass in null Dispatch object");
        }
        if (!theOneInQuestion.isAlive()) {
            throw new IllegalStateException("Dispatch not hooked to windows memory");
        }

    }

    /**
     * not implemented yet
     *
     * @param dispatchTarget
     * @param name
     * @param val
     * @throws com.jacob.com.NotImplementedException
     */
    public static void put_Casesensitive(Dispatch dispatchTarget, String name,
            Object val) {
        throw new NotImplementedException("not implemented yet");
    }

    /*
     * ============================================================ start of the
     * invokev section
     * ===========================================================
     */
    // eliminate _Guid arg
    /**
     * @param dispatchTarget
     * @param name
     * @param dispID
     * @param lcid
     * @param wFlags
     * @param vArg
     * @param uArgErr
     */
    public static void invokeSubv(Dispatch dispatchTarget, String name,
            int dispID, int lcid, int wFlags, Variant[] vArg, int[] uArgErr) {
        throwIfUnattachedDispatch(dispatchTarget);
        invokev(dispatchTarget.pointer.get(), name, dispID, lcid, wFlags, vArg, uArgErr);
    }

    /**
     * @param dispatchTarget
     * @param name
     * @param wFlags
     * @param vArg
     * @param uArgErr
     */
    public static void invokeSubv(Dispatch dispatchTarget, String name,
            int wFlags, Variant[] vArg, int[] uArgErr) {
        throwIfUnattachedDispatch(dispatchTarget);
        invokev(dispatchTarget.pointer.get(), name, 0, Dispatch.LOCALE_SYSTEM_DEFAULT,
                wFlags, vArg, uArgErr);
    }

    /**
     * @param dispatchTarget
     * @param dispID
     * @param wFlags
     * @param vArg
     * @param uArgErr
     */
    public static void invokeSubv(Dispatch dispatchTarget, int dispID,
            int wFlags, Variant[] vArg, int[] uArgErr) {
        throwIfUnattachedDispatch(dispatchTarget);
        invokev(dispatchTarget.pointer.get(), null, dispID, Dispatch.LOCALE_SYSTEM_DEFAULT,
                wFlags, vArg, uArgErr);
    }

    /**
     * not implemented yet
     *
     * @param dispatchTarget
     * @param name
     * @param values
     * @return never returns anything because
     * @throws com.jacob.com.NotImplementedException
     */
    public static Variant callN_CaseSensitive(Dispatch dispatchTarget,
            String name, Object[] values) {
        throw new NotImplementedException("not implemented yet");
    }

    /**
     * @param dispatchTarget
     * @param name
     * @param args
     *            an array of argument objects
     */
    public static void callSubN(Dispatch dispatchTarget, String name,
            Object[] args) {
        throwIfUnattachedDispatch(dispatchTarget);
        invokeSubv(dispatchTarget, name, Dispatch.Method | Dispatch.Get,
                VariantUtilities.objectsToVariants(args),
                new int[args.length]);
    }

    /**
     * @param dispatchTarget
     * @param dispID
     * @param args
     *            an array of argument objects
     */
    public static void callSubN(Dispatch dispatchTarget, int dispID,
            Object[] args) {
        throwIfUnattachedDispatch(dispatchTarget);
        invokeSubv(dispatchTarget, dispID, Dispatch.Method | Dispatch.Get,
                VariantUtilities.objectsToVariants(args),
                new int[args.length]);
    }

    /*
     * ============================================================ start of the
     * getIdsOfNames section
     * ===========================================================
     */
    /**
     * @param dispatchTarget
     * @param name
     * @return int id for the passed in name
     */
    public static int getIDOfName(Dispatch dispatchTarget, String name) {
        int ids[] = getIDsOfNames(dispatchTarget,
                Dispatch.LOCALE_SYSTEM_DEFAULT, new String[]{name});
        return ids[0];
    }

    /**
     * @param dispatchTarget
     * @param lcid
     * @param names
     * @return int[] in id array for passed in names
     */
    // eliminated _Guid argument
    public static int[] getIDsOfNames(Dispatch dispatchTarget, int lcid,
            String[] names) {
        return getIDsOfNames(dispatchTarget.pointer.get(), dispatchTarget, lcid, names);
    }
    private static native int[] getIDsOfNames(int pointer, Dispatch dispatchTarget, int lcid,
            String[] names);

    /**
     * @param dispatchTarget
     * @param names
     * @return int[] int id array for passed in names
     */
    // eliminated _Guid argument
    public static int[] getIDsOfNames(Dispatch dispatchTarget, String[] names) {
        return getIDsOfNames(dispatchTarget, Dispatch.LOCALE_SYSTEM_DEFAULT,
                names);
    }

    /*
     * ============================================================ start of the
     * invokev section
     * ===========================================================
     */
    /**
     * @param dispatchTarget
     * @param name
     * @param args
     * @return Variant returned by call
     */
    public static Variant callN(Dispatch dispatchTarget, String name,
            Object[] args) {
        throwIfUnattachedDispatch(dispatchTarget);
        return invokev(dispatchTarget, name, Dispatch.Method | Dispatch.Get,
                VariantUtilities.objectsToVariants(args),
                new int[args.length]);
    }

    /**
     * @param dispatchTarget
     * @param dispID
     * @param args
     * @return Variant returned by call
     */
    public static Variant callN(Dispatch dispatchTarget, int dispID,
            Object[] args) {
        throwIfUnattachedDispatch(dispatchTarget);
        return invokev(dispatchTarget, dispID, Dispatch.Method | Dispatch.Get,
                VariantUtilities.objectsToVariants(args),
                new int[args.length]);
    }

    /**
     * @param dispatchTarget
     * @param name
     * @param dispID
     * @param lcid
     * @param wFlags
     * @param oArg
     * @param uArgErr
     * @return Variant returned by invoke
     */
    public static Variant invoke(Dispatch dispatchTarget, String name,
            int dispID, int lcid, int wFlags, Object[] oArg, int[] uArgErr) {
        throwIfUnattachedDispatch(dispatchTarget);
        return invokev(dispatchTarget.pointer.get(), name, dispID, lcid, wFlags,
                VariantUtilities.objectsToVariants(oArg), uArgErr);
    }

    /**
     * @param dispatchTarget
     * @param name
     * @param wFlags
     * @param oArg
     * @param uArgErr
     * @return Variant returned by invoke
     */
    public static Variant invoke(Dispatch dispatchTarget, String name,
            int wFlags, Object[] oArg, int[] uArgErr) {
        throwIfUnattachedDispatch(dispatchTarget);
        return invokev(dispatchTarget, name, wFlags, VariantUtilities.objectsToVariants(oArg), uArgErr);
    }

    /**
     * @param dispatchTarget
     * @param dispID
     * @param wFlags
     * @param oArg
     * @param uArgErr
     * @return Variant returned by invoke
     */
    public static Variant invoke(Dispatch dispatchTarget, int dispID,
            int wFlags, Object[] oArg, int[] uArgErr) {
        throwIfUnattachedDispatch(dispatchTarget);
        return invokev(dispatchTarget, dispID, wFlags, VariantUtilities.objectsToVariants(oArg), uArgErr);
    }

    /*
     * ============================================================ start of the
     * callN section ===========================================================
     */

    public static Object callO(Dispatch dispatchTarget, String name) {
        throwIfUnattachedDispatch(dispatchTarget);
        return invokev0(dispatchTarget.pointer.get(), name, 0,
                Dispatch.LOCALE_SYSTEM_DEFAULT, Dispatch.Method | Dispatch.Get);
    }

    public static Object callO(Dispatch dispatchTarget, int dispid) {
        throwIfUnattachedDispatch(dispatchTarget);
        return invokev0(dispatchTarget.pointer.get(), null, dispid,
                Dispatch.LOCALE_SYSTEM_DEFAULT, Dispatch.Method | Dispatch.Get);
    }

    /**
     * @param dispatchTarget
     * @param name
     * @return Variant returned by underlying callN
     */
    public static Variant call(Dispatch dispatchTarget, String name) {
        throwIfUnattachedDispatch(dispatchTarget);
        return callN(dispatchTarget, name, NO_OBJECT_ARGS);
    }

    /**
     * @param dispatchTarget
     * @param name
     * @param a1
     * @return Variant returned by underlying callN
     */
    public static Variant call(Dispatch dispatchTarget, String name, Object a1) {
        throwIfUnattachedDispatch(dispatchTarget);
        return callN(dispatchTarget, name, new Object[]{a1});
    }

    /**
     * @param dispatchTarget
     * @param name
     * @param a1
     * @param a2
     * @return Variant returned by underlying callN
     */
    public static Variant call(Dispatch dispatchTarget, String name, Object a1,
            Object a2) {
        throwIfUnattachedDispatch(dispatchTarget);
        return callN(dispatchTarget, name, new Object[]{a1, a2});
    }

    /**
     * @param dispatchTarget
     * @param name
     * @param a1
     * @param a2
     * @param a3
     * @return Variant returned by underlying callN
     */
    public static Variant call(Dispatch dispatchTarget, String name, Object a1,
            Object a2, Object a3) {
        throwIfUnattachedDispatch(dispatchTarget);
        return callN(dispatchTarget, name, new Object[]{a1, a2, a3});
    }

    /**
     * @param dispatchTarget
     * @param name
     * @param a1
     * @param a2
     * @param a3
     * @param a4
     * @return Variant retuned by underlying callN
     */
    public static Variant call(Dispatch dispatchTarget, String name, Object a1,
            Object a2, Object a3, Object a4) {
        throwIfUnattachedDispatch(dispatchTarget);
        return callN(dispatchTarget, name, new Object[]{a1, a2, a3, a4});
    }

    /**
     * @param dispatchTarget
     * @param name
     * @param a1
     * @param a2
     * @param a3
     * @param a4
     * @param a5
     * @return Variant returned by underlying callN
     */
    public static Variant call(Dispatch dispatchTarget, String name, Object a1,
            Object a2, Object a3, Object a4, Object a5) {
        throwIfUnattachedDispatch(dispatchTarget);
        return callN(dispatchTarget, name, new Object[]{a1, a2, a3, a4, a5});
    }

    /**
     * @param dispatchTarget
     * @param name
     * @param a1
     * @param a2
     * @param a3
     * @param a4
     * @param a5
     * @param a6
     * @return Variant retuned by underlying callN
     */
    public static Variant call(Dispatch dispatchTarget, String name, Object a1,
            Object a2, Object a3, Object a4, Object a5, Object a6) {
        throwIfUnattachedDispatch(dispatchTarget);
        return callN(dispatchTarget, name, new Object[]{a1, a2, a3, a4, a5,
                    a6});
    }

    /**
     * @param dispatchTarget
     * @param name
     * @param a1
     * @param a2
     * @param a3
     * @param a4
     * @param a5
     * @param a6
     * @param a7
     * @return Variant returned by underlying callN
     */
    public static Variant call(Dispatch dispatchTarget, String name, Object a1,
            Object a2, Object a3, Object a4, Object a5, Object a6, Object a7) {
        throwIfUnattachedDispatch(dispatchTarget);
        return callN(dispatchTarget, name, new Object[]{a1, a2, a3, a4, a5,
                    a6, a7});
    }

    /**
     * @param dispatchTarget
     * @param name
     * @param a1
     * @param a2
     * @param a3
     * @param a4
     * @param a5
     * @param a6
     * @param a7
     * @param a8
     * @return Variant retuned by underlying callN
     */
    public static Variant call(Dispatch dispatchTarget, String name, Object a1,
            Object a2, Object a3, Object a4, Object a5, Object a6, Object a7,
            Object a8) {
        throwIfUnattachedDispatch(dispatchTarget);
        return callN(dispatchTarget, name, new Object[]{a1, a2, a3, a4, a5,
                    a6, a7, a8});
    }

    /**
     * @param dispatchTarget
     * @param dispid
     * @return Variant returned by underlying callN
     */
    public static Variant call(Dispatch dispatchTarget, int dispid) {
        throwIfUnattachedDispatch(dispatchTarget);
        return callN(dispatchTarget, dispid, NO_VARIANT_ARGS);
    }

    /**
     * @param dispatchTarget
     * @param dispid
     * @param a1
     * @return Variant returned by underlying callN
     */
    public static Variant call(Dispatch dispatchTarget, int dispid, Object a1) {
        throwIfUnattachedDispatch(dispatchTarget);
        return callN(dispatchTarget, dispid, new Object[]{a1});
    }

    /**
     * @param dispatchTarget
     * @param dispid
     * @param a1
     * @param a2
     * @return Variant returned by underlying callN
     */
    public static Variant call(Dispatch dispatchTarget, int dispid, Object a1,
            Object a2) {
        throwIfUnattachedDispatch(dispatchTarget);
        return callN(dispatchTarget, dispid, new Object[]{a1, a2});
    }

    /**
     * @param dispatchTarget
     * @param dispid
     * @param a1
     * @param a2
     * @param a3
     * @return Variant returned by underlying callN
     */
    public static Variant call(Dispatch dispatchTarget, int dispid, Object a1,
            Object a2, Object a3) {
        throwIfUnattachedDispatch(dispatchTarget);
        return callN(dispatchTarget, dispid, new Object[]{a1, a2, a3});
    }

    /**
     * @param dispatchTarget
     * @param dispid
     * @param a1
     * @param a2
     * @param a3
     * @param a4
     * @return Variant returned by underlying callN
     */
    public static Variant call(Dispatch dispatchTarget, int dispid, Object a1,
            Object a2, Object a3, Object a4) {
        throwIfUnattachedDispatch(dispatchTarget);
        return callN(dispatchTarget, dispid, new Object[]{a1, a2, a3, a4});
    }

    /**
     * @param dispatchTarget
     * @param dispid
     * @param a1
     * @param a2
     * @param a3
     * @param a4
     * @param a5
     * @return Variant returned by underlying callN
     */
    public static Variant call(Dispatch dispatchTarget, int dispid, Object a1,
            Object a2, Object a3, Object a4, Object a5) {
        throwIfUnattachedDispatch(dispatchTarget);
        return callN(dispatchTarget, dispid,
                new Object[]{a1, a2, a3, a4, a5});
    }

    /**
     * @param dispatchTarget
     * @param dispid
     * @param a1
     * @param a2
     * @param a3
     * @param a4
     * @param a5
     * @param a6
     * @return Variant returned by underlying callN
     */
    public static Variant call(Dispatch dispatchTarget, int dispid, Object a1,
            Object a2, Object a3, Object a4, Object a5, Object a6) {
        throwIfUnattachedDispatch(dispatchTarget);
        return callN(dispatchTarget, dispid, new Object[]{a1, a2, a3, a4, a5,
                    a6});
    }

    /**
     * @param dispatchTarget
     * @param dispid
     * @param a1
     * @param a2
     * @param a3
     * @param a4
     * @param a5
     * @param a6
     * @param a7
     * @return Variant returned by underlying callN
     */
    public static Variant call(Dispatch dispatchTarget, int dispid, Object a1,
            Object a2, Object a3, Object a4, Object a5, Object a6, Object a7) {
        throwIfUnattachedDispatch(dispatchTarget);
        return callN(dispatchTarget, dispid, new Object[]{a1, a2, a3, a4, a5,
                    a6, a7});
    }

    /**
     * @param dispatchTarget
     * @param dispid
     * @param a1
     * @param a2
     * @param a3
     * @param a4
     * @param a5
     * @param a6
     * @param a7
     * @param a8
     * @return Variant returned by underlying callN
     */
    public static Variant call(Dispatch dispatchTarget, int dispid, Object a1,
            Object a2, Object a3, Object a4, Object a5, Object a6, Object a7,
            Object a8) {
        throwIfUnattachedDispatch(dispatchTarget);
        return callN(dispatchTarget, dispid, new Object[]{a1, a2, a3, a4, a5,
                    a6, a7, a8});
    }

    /*
     * ============================================================ start of the
     * invoke section
     * ===========================================================
     */
    /**
     * @param dispatchTarget
     * @param name
     * @param val
     */
    public static void put(Dispatch dispatchTarget, String name, Object val) {
        throwIfUnattachedDispatch(dispatchTarget);
        invoke(dispatchTarget, name, Dispatch.Put, new Object[]{val},
                new int[1]);
    }

    /**
     * @param dispatchTarget
     * @param dispid
     * @param val
     */
    public static void put(Dispatch dispatchTarget, int dispid, Object val) {
        throwIfUnattachedDispatch(dispatchTarget);
        invoke(dispatchTarget, dispid, Dispatch.Put, new Object[]{val},
                new int[1]);
    }

    /*
     * ============================================================ start of the
     * invokev section
     * ===========================================================
     */
    // removed _Guid argument
    /**
     * @param dispatchTarget
     * @param name
     * @param dispID
     * @param lcid
     * @param wFlags
     * @param vArg
     * @param uArgErr
     * @return Variant returned by underlying invokev
     */
    public static Variant invokev(Dispatch dispatchTarget, String name,
            int dispID, int lcid, int wFlags, Variant[] vArg, int[] uArgErr) {
        return invokev(dispatchTarget.pointer.get(), name, dispID, lcid, wFlags, vArg, uArgErr);
    }

    private static native Object invokev0(int pointer, String name,
            int dispID, int lcid, int wFlags);

    private static native Variant invokev(int pointer, String name,
            int dispID, int lcid, int wFlags, Variant[] vArg, int[] uArgErr);

    /**
     * @param dispatchTarget
     * @param name
     * @param wFlags
     * @param vArg
     * @param uArgErr
     * @return Variant returned by underlying invokev
     */
    public static Variant invokev(Dispatch dispatchTarget, String name,
            int wFlags, Variant[] vArg, int[] uArgErr) {
        throwIfUnattachedDispatch(dispatchTarget);
        return invokev(dispatchTarget.pointer.get(), name, 0, Dispatch.LOCALE_SYSTEM_DEFAULT,
                wFlags, vArg, uArgErr);
    }

    /**
     * @param dispatchTarget
     * @param name
     * @param wFlags
     * @param vArg
     * @param uArgErr
     * @param wFlagsEx
     * @return Variant returned by underlying invokev
     */
    public static Variant invokev(Dispatch dispatchTarget, String name,
            int wFlags, Variant[] vArg, int[] uArgErr, int wFlagsEx) {
        throwIfUnattachedDispatch(dispatchTarget);
        // do not implement IDispatchEx for now
        return invokev(dispatchTarget.pointer.get(), name, 0, Dispatch.LOCALE_SYSTEM_DEFAULT,
                wFlags, vArg, uArgErr);
    }

    /**
     * @param dispatchTarget
     * @param dispID
     * @param wFlags
     * @param vArg
     * @param uArgErr
     * @return Variant returned by underlying invokev
     */
    public static Variant invokev(Dispatch dispatchTarget, int dispID,
            int wFlags, Variant[] vArg, int[] uArgErr) {
        throwIfUnattachedDispatch(dispatchTarget);
        return invokev(dispatchTarget.pointer.get(), null, dispID,
                Dispatch.LOCALE_SYSTEM_DEFAULT, wFlags, vArg, uArgErr);
    }

    /*
     * ============================================================ start of the
     * invokeSubv section
     * ===========================================================
     */
    // removed _Guid argument
    /**
     * @param dispatchTarget
     * @param name
     * @param dispid
     * @param lcid
     * @param wFlags
     * @param oArg
     * @param uArgErr
     */
    public static void invokeSub(Dispatch dispatchTarget, String name,
            int dispid, int lcid, int wFlags, Object[] oArg, int[] uArgErr) {
        throwIfUnattachedDispatch(dispatchTarget);
        invokeSubv(dispatchTarget, name, dispid, lcid, wFlags,
                VariantUtilities.objectsToVariants(oArg), uArgErr);
    }

    /*
     * ============================================================ start of the
     * invokeSub section
     * ===========================================================
     */
    /**
     * @param dispatchTarget
     * @param name
     * @param wFlags
     * @param oArg
     * @param uArgErr
     */
    public static void invokeSub(Dispatch dispatchTarget, String name,
            int wFlags, Object[] oArg, int[] uArgErr) {
        throwIfUnattachedDispatch(dispatchTarget);
        invokeSub(dispatchTarget, name, 0, Dispatch.LOCALE_SYSTEM_DEFAULT,
                wFlags, oArg, uArgErr);
    }

    /**
     * @param dispatchTarget
     * @param dispid
     * @param wFlags
     * @param oArg
     * @param uArgErr
     */
    public static void invokeSub(Dispatch dispatchTarget, int dispid,
            int wFlags, Object[] oArg, int[] uArgErr) {
        throwIfUnattachedDispatch(dispatchTarget);
        invokeSub(dispatchTarget, null, dispid, Dispatch.LOCALE_SYSTEM_DEFAULT,
                wFlags, oArg, uArgErr);
    }

    /*
     * ============================================================ start of the
     * callSubN section
     * ===========================================================
     */
    /**
     * makes call to native callSubN
     *
     * @param dispatchTarget
     * @param name
     */
    public static void callSub(Dispatch dispatchTarget, String name) {
        throwIfUnattachedDispatch(dispatchTarget);
        callSubN(dispatchTarget, name, NO_OBJECT_ARGS);
    }

    /**
     * makes call to native callSubN
     *
     * @param dispatchTarget
     * @param name
     * @param a1
     */
    public static void callSub(Dispatch dispatchTarget, String name, Object a1) {
        throwIfUnattachedDispatch(dispatchTarget);
        callSubN(dispatchTarget, name, new Object[]{a1});
    }

    /**
     * makes call to native callSubN
     *
     * @param dispatchTarget
     * @param name
     * @param a1
     * @param a2
     */
    public static void callSub(Dispatch dispatchTarget, String name, Object a1,
            Object a2) {
        throwIfUnattachedDispatch(dispatchTarget);
        callSubN(dispatchTarget, name, new Object[]{a1, a2});
    }

    /**
     * makes call to native callSubN
     *
     * @param dispatchTarget
     * @param name
     * @param a1
     * @param a2
     * @param a3
     */
    public static void callSub(Dispatch dispatchTarget, String name, Object a1,
            Object a2, Object a3) {
        throwIfUnattachedDispatch(dispatchTarget);
        callSubN(dispatchTarget, name, new Object[]{a1, a2, a3});
    }

    /**
     * makes call to native callSubN
     *
     * @param dispatchTarget
     * @param name
     * @param a1
     * @param a2
     * @param a3
     * @param a4
     */
    public static void callSub(Dispatch dispatchTarget, String name, Object a1,
            Object a2, Object a3, Object a4) {
        throwIfUnattachedDispatch(dispatchTarget);
        callSubN(dispatchTarget, name, new Object[]{a1, a2, a3, a4});
    }

    /**
     * makes call to native callSubN
     *
     * @param dispatchTarget
     * @param name
     * @param a1
     * @param a2
     * @param a3
     * @param a4
     * @param a5
     */
    public static void callSub(Dispatch dispatchTarget, String name, Object a1,
            Object a2, Object a3, Object a4, Object a5) {
        throwIfUnattachedDispatch(dispatchTarget);
        callSubN(dispatchTarget, name, new Object[]{a1, a2, a3, a4, a5});
    }

    /**
     * makes call to native callSubN
     *
     * @param dispatchTarget
     * @param name
     * @param a1
     * @param a2
     * @param a3
     * @param a4
     * @param a5
     * @param a6
     */
    public static void callSub(Dispatch dispatchTarget, String name, Object a1,
            Object a2, Object a3, Object a4, Object a5, Object a6) {
        throwIfUnattachedDispatch(dispatchTarget);
        callSubN(dispatchTarget, name, new Object[]{a1, a2, a3, a4, a5, a6});
    }

    /**
     * makes call to native callSubN
     *
     * @param dispatchTarget
     * @param name
     * @param a1
     * @param a2
     * @param a3
     * @param a4
     * @param a5
     * @param a6
     * @param a7
     */
    public static void callSub(Dispatch dispatchTarget, String name, Object a1,
            Object a2, Object a3, Object a4, Object a5, Object a6, Object a7) {
        throwIfUnattachedDispatch(dispatchTarget);
        callSubN(dispatchTarget, name, new Object[]{a1, a2, a3, a4, a5, a6,
                    a7});
    }

    /**
     * makes call to native callSubN
     *
     * @param dispatchTarget
     * @param name
     * @param a1
     * @param a2
     * @param a3
     * @param a4
     * @param a5
     * @param a6
     * @param a7
     * @param a8
     */
    public static void callSub(Dispatch dispatchTarget, String name, Object a1,
            Object a2, Object a3, Object a4, Object a5, Object a6, Object a7,
            Object a8) {
        throwIfUnattachedDispatch(dispatchTarget);
        callSubN(dispatchTarget, name, new Object[]{a1, a2, a3, a4, a5, a6,
                    a7, a8});
    }

    /**
     * makes call to native callSubN
     *
     * @param dispatchTarget
     * @param dispid
     */
    public static void callSub(Dispatch dispatchTarget, int dispid) {
        throwIfUnattachedDispatch(dispatchTarget);
        callSubN(dispatchTarget, dispid, NO_OBJECT_ARGS);
    }

    /**
     * makes call to native callSubN
     *
     * @param dispatchTarget
     * @param dispid
     * @param a1
     */
    public static void callSub(Dispatch dispatchTarget, int dispid, Object a1) {
        throwIfUnattachedDispatch(dispatchTarget);
        callSubN(dispatchTarget, dispid, new Object[]{a1});
    }

    /**
     * makes call to native callSubN
     *
     * @param dispatchTarget
     * @param dispid
     * @param a1
     * @param a2
     */
    public static void callSub(Dispatch dispatchTarget, int dispid, Object a1,
            Object a2) {
        throwIfUnattachedDispatch(dispatchTarget);
        callSubN(dispatchTarget, dispid, new Object[]{a1, a2});
    }

    /**
     * makes call to native callSubN
     *
     * @param dispatchTarget
     * @param dispid
     * @param a1
     * @param a2
     * @param a3
     */
    public static void callSub(Dispatch dispatchTarget, int dispid, Object a1,
            Object a2, Object a3) {
        callSubN(dispatchTarget, dispid, new Object[]{a1, a2, a3});
    }

    /**
     * makes call to native callSubN
     *
     * @param dispatchTarget
     * @param dispid
     * @param a1
     * @param a2
     * @param a3
     * @param a4
     */
    public static void callSub(Dispatch dispatchTarget, int dispid, Object a1,
            Object a2, Object a3, Object a4) {
        throwIfUnattachedDispatch(dispatchTarget);
        callSubN(dispatchTarget, dispid, new Object[]{a1, a2, a3, a4});
    }

    /**
     * makes call to native callSubN
     *
     * @param dispatchTarget
     * @param dispid
     * @param a1
     * @param a2
     * @param a3
     * @param a4
     * @param a5
     */
    public static void callSub(Dispatch dispatchTarget, int dispid, Object a1,
            Object a2, Object a3, Object a4, Object a5) {
        throwIfUnattachedDispatch(dispatchTarget);
        callSubN(dispatchTarget, dispid, new Object[]{a1, a2, a3, a4, a5});
    }

    /**
     * makes call to native callSubN
     *
     * @param dispatchTarget
     * @param dispid
     * @param a1
     * @param a2
     * @param a3
     * @param a4
     * @param a5
     * @param a6
     */
    public static void callSub(Dispatch dispatchTarget, int dispid, Object a1,
            Object a2, Object a3, Object a4, Object a5, Object a6) {
        throwIfUnattachedDispatch(dispatchTarget);
        callSubN(dispatchTarget, dispid,
                new Object[]{a1, a2, a3, a4, a5, a6});
    }

    /**
     * makes call to native callSubN
     *
     * @param dispatchTarget
     * @param dispid
     * @param a1
     * @param a2
     * @param a3
     * @param a4
     * @param a5
     * @param a6
     * @param a7
     */
    public static void callSub(Dispatch dispatchTarget, int dispid, Object a1,
            Object a2, Object a3, Object a4, Object a5, Object a6, Object a7) {
        throwIfUnattachedDispatch(dispatchTarget);
        callSubN(dispatchTarget, dispid, new Object[]{a1, a2, a3, a4, a5, a6,
                    a7});
    }

    /**
     * makes call to native callSubN
     *
     * @param dispatchTarget
     * @param dispid
     * @param a1
     * @param a2
     * @param a3
     * @param a4
     * @param a5
     * @param a6
     * @param a7
     * @param a8
     */
    public static void callSub(Dispatch dispatchTarget, int dispid, Object a1,
            Object a2, Object a3, Object a4, Object a5, Object a6, Object a7,
            Object a8) {
        callSubN(dispatchTarget, dispid, new Object[]{a1, a2, a3, a4, a5, a6,
                    a7, a8});
    }

    /*
     * ============================================================ start of the
     * invokev section
     * ===========================================================
     */
    /**
     * Cover for call to underlying invokev()
     *
     * @param dispatchTarget
     * @param name
     * @return Variant returned by the request for retrieval of parameter
     */
    public static Variant get(Dispatch dispatchTarget, String name) {
        throwIfUnattachedDispatch(dispatchTarget);
        return invokev(dispatchTarget, name, Dispatch.Get, NO_VARIANT_ARGS,
                NO_INT_ARGS);
    }

    /**
     * Cover for call to underlying invokev()
     *
     * @param dispatchTarget
     * @param dispid
     * @return Variant returned by the request for retrieval of parameter
     */
    public static Variant get(Dispatch dispatchTarget, int dispid) {
        throwIfUnattachedDispatch(dispatchTarget);
        return invokev(dispatchTarget, dispid, Dispatch.Get, NO_VARIANT_ARGS,
                NO_INT_ARGS);
    }

    /*
     * ============================================================ start of the
     * invoke section
     * ===========================================================
     */
    /**
     * cover for underlying call to invoke
     *
     * @param dispatchTarget
     * @param name
     * @param val
     */
    public static void putRef(Dispatch dispatchTarget, String name, Object val) {
        throwIfUnattachedDispatch(dispatchTarget);
        invoke(dispatchTarget, name, Dispatch.PutRef, new Object[]{val},
                new int[1]);
    }

    /**
     * cover for underlying call to invoke
     *
     * @param dispatchTarget
     * @param dispid
     * @param val
     */
    public static void putRef(Dispatch dispatchTarget, int dispid, Object val) {
        throwIfUnattachedDispatch(dispatchTarget);
        invoke(dispatchTarget, dispid, Dispatch.PutRef, new Object[]{val},
                new int[1]);
    }

    /**
     * not implemented yet
     *
     * @param dispatchTarget
     * @param name
     * @return Variant never returned
     * @throws com.jacob.com.NotImplementedException
     */
    public static Variant get_CaseSensitive(Dispatch dispatchTarget, String name) {
        throw new NotImplementedException("not implemented yet");
    }
}