#!/usr/bin/env python3
"""Tiny adb driver. usage: adrive.py tap "<text>" | shot <file> | log | texts"""
import subprocess, sys, re, os, time
ADB = os.path.expanduser("~/Library/Android/sdk/platform-tools/adb")
def dump():
    subprocess.run([ADB,"shell","uiautomator","dump","/sdcard/ui.xml"],capture_output=True)
    return subprocess.run([ADB,"shell","cat","/sdcard/ui.xml"],capture_output=True,text=True).stdout
def nodes(xml):
    for m in re.finditer(r'<node [^>]*?text="([^"]*)"[^>]*?content-desc="([^"]*)"[^>]*?bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"', xml):
        t,d,x1,y1,x2,y2=m.groups(); yield (t or d), (int(x1)+int(x2))//2, (int(y1)+int(y2))//2
cmd=sys.argv[1]
if cmd=="tap":
    want=sys.argv[2]
    for _ in range(10):
        hits=[n for n in nodes(dump()) if n[0]==want] or [n for n in nodes(dump()) if want.lower() in n[0].lower()]
        if hits:
            _,x,y=hits[0]; subprocess.run([ADB,"shell","input","tap",str(x),str(y)]); print(f"tapped '{hits[0][0]}' at {x},{y}"); break
        time.sleep(1)
    else: print(f"NOT FOUND: {want}"); sys.exit(1)
elif cmd=="texts":
    print([n[0] for n in nodes(dump()) if n[0]])
elif cmd=="scroll":
    subprocess.run([ADB,"shell","input","swipe","1300","2500","1300","900","600"])
elif cmd=="shot":
    open(sys.argv[2],"wb").write(subprocess.run([ADB,"exec-out","screencap","-p"],capture_output=True).stdout)
elif cmd=="log":
    out=subprocess.run([ADB,"logcat","-d","-s","Gr4vySDK"],capture_output=True,text=True).stdout
    for l in out.splitlines():
        if re.search(r"\[NETWORK\]|204|versioning|Indicator|[Cc]hallenge|cancel|ERROR|attempt", l) and not re.search(r'sdk_encrypted|Bearer|"sdk_', l):
            print(l.split("Gr4vySDK")[-1][:220])
