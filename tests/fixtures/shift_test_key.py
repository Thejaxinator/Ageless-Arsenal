"""Send Shift to the disposable test client's X display so vanilla's real physical-key test runs."""
import ctypes as C
import ctypes.util
import os
import sys
x=C.CDLL(ctypes.util.find_library('X11'))
xt=C.CDLL(ctypes.util.find_library('Xtst'))
x.XOpenDisplay.argtypes=[C.c_char_p]
x.XOpenDisplay.restype=C.c_void_p
d=x.XOpenDisplay(os.environ['DISPLAY'].encode())
if not d:raise RuntimeError('test display missing')
# A bare X server has no window manager to assign focus to Minecraft's top-level window.
# Choose its visible client from the root tree before sending the physical key.
x.XDefaultRootWindow.argtypes=[C.c_void_p];x.XDefaultRootWindow.restype=C.c_ulong
root=x.XDefaultRootWindow(d)
x.XQueryTree.argtypes=[C.c_void_p,C.c_ulong,C.POINTER(C.c_ulong),C.POINTER(C.c_ulong),C.POINTER(C.POINTER(C.c_ulong)),C.POINTER(C.c_uint)]
r=C.c_ulong();parent=C.c_ulong();children=C.POINTER(C.c_ulong)();count=C.c_uint()
x.XQueryTree(d,root,C.byref(r),C.byref(parent),C.byref(children),C.byref(count))
x.XFetchName.argtypes=[C.c_void_p,C.c_ulong,C.POINTER(C.c_char_p)]
x.XSetInputFocus.argtypes=[C.c_void_p,C.c_ulong,C.c_int,C.c_ulong]
for i in range(count.value):
    title=C.c_char_p();child=children[i]
    if x.XFetchName(d,child,C.byref(title)) and title.value and b'Minecraft' in title.value:
        x.XSetInputFocus(d,child,2,0)
        break
x.XKeysymToKeycode.argtypes=[C.c_void_p,C.c_ulong]
x.XKeysymToKeycode.restype=C.c_uint
shift=x.XKeysymToKeycode(d,0xffe1)
xt.XTestFakeKeyEvent.argtypes=[C.c_void_p,C.c_uint,C.c_int,C.c_ulong]
assert xt.XTestFakeKeyEvent(d,shift,int(sys.argv[1]=='down'),0)
x.XSync.argtypes=[C.c_void_p,C.c_int]
x.XSync(d,0)
x.XCloseDisplay.argtypes=[C.c_void_p]
x.XCloseDisplay(d)
