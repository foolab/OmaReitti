/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: /home/nechaev/projects/omareitti/src/com/omareitti/IBackgroundServiceAPI.aidl
 */
package com.omareitti;
public interface IBackgroundServiceAPI extends android.os.IInterface
{
/** Local-side IPC implementation stub class. */
public static abstract class Stub extends android.os.Binder implements com.omareitti.IBackgroundServiceAPI
{
private static final java.lang.String DESCRIPTOR = "com.omareitti.IBackgroundServiceAPI";
/** Construct the stub at attach it to the interface. */
public Stub()
{
this.attachInterface(this, DESCRIPTOR);
}
/**
 * Cast an IBinder object into an com.omareitti.IBackgroundServiceAPI interface,
 * generating a proxy if needed.
 */
public static com.omareitti.IBackgroundServiceAPI asInterface(android.os.IBinder obj)
{
if ((obj==null)) {
return null;
}
android.os.IInterface iin = (android.os.IInterface)obj.queryLocalInterface(DESCRIPTOR);
if (((iin!=null)&&(iin instanceof com.omareitti.IBackgroundServiceAPI))) {
return ((com.omareitti.IBackgroundServiceAPI)iin);
}
return new com.omareitti.IBackgroundServiceAPI.Stub.Proxy(obj);
}
public android.os.IBinder asBinder()
{
return this;
}
@Override public boolean onTransact(int code, android.os.Parcel data, android.os.Parcel reply, int flags) throws android.os.RemoteException
{
switch (code)
{
case INTERFACE_TRANSACTION:
{
reply.writeString(DESCRIPTOR);
return true;
}
case TRANSACTION_requestLastKnownAddress:
{
data.enforceInterface(DESCRIPTOR);
int _arg0;
_arg0 = data.readInt();
int _result = this.requestLastKnownAddress(_arg0);
reply.writeNoException();
reply.writeInt(_result);
return true;
}
case TRANSACTION_isGPSOn:
{
data.enforceInterface(DESCRIPTOR);
boolean _result = this.isGPSOn();
reply.writeNoException();
reply.writeInt(((_result)?(1):(0)));
return true;
}
case TRANSACTION_setRoute:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
boolean _arg1;
_arg1 = (0!=data.readInt());
boolean _arg2;
_arg2 = (0!=data.readInt());
this.setRoute(_arg0, _arg1, _arg2);
reply.writeNoException();
return true;
}
case TRANSACTION_cancelRoute:
{
data.enforceInterface(DESCRIPTOR);
int _arg0;
_arg0 = data.readInt();
this.cancelRoute(_arg0);
reply.writeNoException();
return true;
}
case TRANSACTION_addListener:
{
data.enforceInterface(DESCRIPTOR);
com.omareitti.IBackgroundServiceListener _arg0;
_arg0 = com.omareitti.IBackgroundServiceListener.Stub.asInterface(data.readStrongBinder());
this.addListener(_arg0);
reply.writeNoException();
return true;
}
case TRANSACTION_removeListener:
{
data.enforceInterface(DESCRIPTOR);
com.omareitti.IBackgroundServiceListener _arg0;
_arg0 = com.omareitti.IBackgroundServiceListener.Stub.asInterface(data.readStrongBinder());
this.removeListener(_arg0);
reply.writeNoException();
return true;
}
}
return super.onTransact(code, data, reply, flags);
}
private static class Proxy implements com.omareitti.IBackgroundServiceAPI
{
private android.os.IBinder mRemote;
Proxy(android.os.IBinder remote)
{
mRemote = remote;
}
public android.os.IBinder asBinder()
{
return mRemote;
}
public java.lang.String getInterfaceDescriptor()
{
return DESCRIPTOR;
}
public int requestLastKnownAddress(int getAddress) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
int _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeInt(getAddress);
mRemote.transact(Stub.TRANSACTION_requestLastKnownAddress, _data, _reply, 0);
_reply.readException();
_result = _reply.readInt();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
public boolean isGPSOn() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
boolean _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_isGPSOn, _data, _reply, 0);
_reply.readException();
_result = (0!=_reply.readInt());
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
public void setRoute(java.lang.String route, boolean remindDep, boolean remindArr) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(route);
_data.writeInt(((remindDep)?(1):(0)));
_data.writeInt(((remindArr)?(1):(0)));
mRemote.transact(Stub.TRANSACTION_setRoute, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
public void cancelRoute(int notify) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeInt(notify);
mRemote.transact(Stub.TRANSACTION_cancelRoute, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
public void addListener(com.omareitti.IBackgroundServiceListener l) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeStrongBinder((((l!=null))?(l.asBinder()):(null)));
mRemote.transact(Stub.TRANSACTION_addListener, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
public void removeListener(com.omareitti.IBackgroundServiceListener l) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeStrongBinder((((l!=null))?(l.asBinder()):(null)));
mRemote.transact(Stub.TRANSACTION_removeListener, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
}
static final int TRANSACTION_requestLastKnownAddress = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
static final int TRANSACTION_isGPSOn = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
static final int TRANSACTION_setRoute = (android.os.IBinder.FIRST_CALL_TRANSACTION + 2);
static final int TRANSACTION_cancelRoute = (android.os.IBinder.FIRST_CALL_TRANSACTION + 3);
static final int TRANSACTION_addListener = (android.os.IBinder.FIRST_CALL_TRANSACTION + 4);
static final int TRANSACTION_removeListener = (android.os.IBinder.FIRST_CALL_TRANSACTION + 5);
}
public int requestLastKnownAddress(int getAddress) throws android.os.RemoteException;
public boolean isGPSOn() throws android.os.RemoteException;
public void setRoute(java.lang.String route, boolean remindDep, boolean remindArr) throws android.os.RemoteException;
public void cancelRoute(int notify) throws android.os.RemoteException;
public void addListener(com.omareitti.IBackgroundServiceListener l) throws android.os.RemoteException;
public void removeListener(com.omareitti.IBackgroundServiceListener l) throws android.os.RemoteException;
}
