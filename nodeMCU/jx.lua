local function o(t)
local e=type(t)
if e=="table"then
local e
for a,t in pairs(t)do
if e then
e=string.format("%s,\"%s\":",e,a)
else
e=string.format("{\"%s\":",a)
end
local a
local o=type(t)
if o=="string"then
a=string.format("\"%s\"",t);
else
a=string.format("%d",t);
end
e=e..a
end
return e.."}"
end
return nil;
end
local function v(t)
local e={}
for t,o,a in string.gfind(t,"%s*\"(%a%w*)\":(\"?)([%a%d\.]*)\"?%s*")do
if o=="\""then
e[t]=a
else
e[t]=tonumber(a)
end
end
return e
end
local r="ipAddress_pi"
local y="wifi_ssid"
local p="wifi_passwd"
local a="myName"
local h=10008
local g="NodeMCU"
local l
local b="1.0.0"
local n
local s
local function t(t,o)
local e={}
e.c=t;
e.mid=o
e.n=a;
return e;
end
local function a(e)
local e=o(e).."\n"
s:send(e)
end
local function d(e)
local e=t("r",e)
e.m="OK"
a(e)
end
local function e(o)
local e=t("d",0)
e.m=o
a(e)
end
local function e(o)
local e=t("e",0)
e.m=o
a(e)
end
local function u(i,o)
local e=t("i",0)
e.t="g"
e.p=i
e.d=o
a(e)
end
local function f(e)
if e.t=="g"then
if e.m==1 then
gpio.mode(e.p,gpio.OUTPUT)
elseif e.m==0 then
if e.f==0 then
gpio.mode(e.p,gpio.INPUT,gpio.FLOAT)
else
gpio.mode(e.p,gpio.INPUT,gpio.PULLUP)
end
elseif e.m==2 then
pwm.setup(e.p,e.o,e.d)
pwm.start(e.p)
elseif e.m==3 then
gpio.mode(e.p,gpio.INT,gpio.PULLUP)
gpio.trig(e.p,"both",function(t)
u(e.p,t)
end)
end
end
d(e.mid)
end
local i=0
local function c(e)
if e.t=="g"then
local t=gpio.HIGH
if e.a==0 then t=gpio.LOW end
gpio.write(e.p,t)
elseif e.t=="p"then
pwm.setduty(e.p,e.d)
end
d(e.mid)
end
local function w(e)
if e.t=="g"then
local t=t("r",e.mid)
t.t="g"
t.p=e.p
t.d=gpio.read(e.p)
a(t)
end
end
local function u()
local e=0
tmr.alarm(1,1e3,1,function()
n=wifi.sta.getip()
if not n and(e<10)then
print("c")
wifi.sta.connect()
e=e+1
else
tmr.stop(1);
print("CR:"..n)
if(e<10)then
print("CS:"..r.."/"..h)
s:connect(h,r)
end
end
end)
end
local function m()
l=node.chipid()
wifi.setmode(wifi.STATION)
wifi.sta.config(y,p)
wifi.sta.connect()
s=net.createConnection(net.TCP,0)
s:on("receive",function(o,e)
print("r "..e)
local e=v(e)
local o=e.c
if not e then return end
if(o=="l")then
i=0;
d(e.mid)
elseif(o=="s")then
c(e)
elseif(o=="g")then
w(e)
elseif(o=="c")then
f(e,true)
elseif(o=="reg")then
local e=t("reg")
e.ID=l;
e.t=g
e.IP=n
e.v=b
a(e)
i=0
end
end)
u()
tmr.alarm(0,1e4,1,function()
i=i+1;
if n then
if i>10 then
print("CS:"..r.."/"..h)
s:connect(h,r)
end
else
u()
end
end)
end
m()