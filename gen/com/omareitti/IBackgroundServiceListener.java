/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: /home/nechaev/projects/omareitti/src/com/omareitti/IBackgroundServiceListener.aidl
 */
package com.omareitti;
public interface IBackgroundServiceListener extends android.os.IInterface
{
/** Local-side IPC implementation stub class. */
public static abstract class Stub extends android.os.Binder implements com.omareitti.IBackgroundServiceListener
{
private static final java.lang.String DESCRIPTOR = "com.omareitti.IBackgroundServiceListener";
/** Construct the stub at attach it to the interface. */
public Stub()
{
this.attachInterface(this, DESCRIPTOR);
}
/**
 * Cast an IBinder object into an com.omareitti.IBackgroundServiceListener interface,
 * generating a proxy if needed.
 */
public static com.omareitti.IBackgroundServiceListener asInterface(android.os.IBinder obj)
{
if ((obj==null)) {
return null;
}
android.os.IInterface iin = (android.os.IInterface)obj.queryLocalInterface(DESCRIPTOR);
if (((iin!=null)&&(iin instanceof com.omareitti.IBackgroundServiceListener))) {
return ((com.omareitti.IBackgroundServiceListener)iin);
}
return new com.omareitti.IBackgroundServiceListener.Stub.Proxy(obj);
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
case TRANSACTION_handleUpdate:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
this.handleUpdate(_arg0);
reply.writeNoException();
return true;
}
case TRANSACTION_handleGPSUpdate:
{
data.enforceInterface(DESCRIPTOR);
double _arg0;
_arg0 = data.readDouble();
double _arg1;
_arg1 = data.readDouble();
float _arg2;
_arg2 = data.readFloat();
this.handleGPSUpdate(_arg0, _arg1, _arg2);
reply.writeNoException();
return true;
}
case TRANSACTION_locationDiscovered:
{
data.enforceInterface(DESCRIPTOR);
double _arg0;
_arg0 = data.readDouble();
double _arg1;
_arg1 = data.readDouble();
this.locationDiscovered(_arg0, _arg1);
reply.writeNoException();
return true;
}
case TRANSACTION_addressDiscovered:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
this.addressDiscovered(_arg0);
reply.writeNoException();
return true;
}
}
return super.onTransact(code, data, reply, flags);
}
private static class Proxy implements com.omareitti.IBackgroundServiceListener
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
public void handleUpdate(java.lang.String s) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(s);
mRemote.transact(Stub.TRANSACTION_handleUpdate, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
public void handleGPSUpdate(double lat, double lon, float angle) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeDouble(lat);
_data.writeDouble(lon);
_data.writeFloat(angle);
mRemote.transact(Stub.TRANSACTION_handleGPSUpdate, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
public void locationDiscovered(double lat, double lon) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeDouble(lat);
_data.writeDouble(lon);
mRemote.transact(Stub.TRANSACTION_locationDiscovered, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
public void addressDiscovered(java.lang.String address) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(address);
mRemote.transact(Stub.TRANSACTION_addressDiscovered, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
}
static final int TRANSACTION_handleUpdate = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
static final int TRANSACTION_handleGPSUpdate = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
static final int TRANSACTION_locationDiscovered = (android.os.IBinder.FIRST_CALL_TRANSACTION + 2);
static final int TRANSACTION_addressDiscovered = (android.os.IBinder.FIRST_CALL_TRANSACTION + 3);
}
public void handleUpdate(java.lang.String s) throws android.os.RemoteException;
public void handleGPSUpdate(double lat, double lon, float angle) throws android.os.RemoteException;
public void locationDiscovered(double lat, double lon) throws android.os.RemoteException;
public void addressDiscovered(java.lang.String address) throws android.os.RemoteException;
}
