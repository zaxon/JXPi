
package cn.ijingxi.common.util;

import cn.ijingxi.common.Process.InstanceState;
import cn.ijingxi.common.orm.ORMID;

import java.text.SimpleDateFormat;
import java.util.*;


public class Trans
{
	public static UUID NullUUID=UUID.fromString("00000000000000000000000000000000");
	public static String DateTimeFormat="yyyy-MM-dd HH:mm:ss";
	public static String DateFormat="yyyy-MM-dd";
	public static SimpleDateFormat DateTimeFormatter=new SimpleDateFormat(DateTimeFormat);
	public static SimpleDateFormat DateFormatter=new SimpleDateFormat(DateFormat);
	
	public static boolean JudgeIsDefaultValue(Class<?> TypeName,Object value)
	{
		if(value==null)return true;
		switch(utils.GetClassName(TypeName))
		{
		case "byte":
		case "Byte":
			return TransToByte(value)==0;
		case "short":
		case "Short":
			return TransToShort(value)==0;
		case "long":
		case "Long":
			return TransToLong(value)==0;
		case "float":
		case "Float":
			return TransToFloat(value)==0;
		case "double":
		case "Double":
			return TransToDouble(value)==0;
		case "boolean":
		case "Boolean":
			return !TransToBoolean(value);
		}
		return true;
	}

	
	public static String TransToString(Date ct)
	{
		if(ct==null)return null;
		 return DateTimeFormatter.format(ct);
	}
	
	public static Object TransFromJavaToJSON(Class<?> cls,Object obj)
	{
		if(obj==null)return null;
		if(utils.JudgeIsEnum(cls))
			return Trans.TransToInteger((Enum<?>)obj);
		else if(utils.GetClassName(cls).compareTo("UUID")==0)
			return Trans.TransToString((UUID)obj);
		else if(utils.GetClassName(cls).compareTo("Date")==0)
			return Trans.TransToInteger((Date)obj);
		else
			return obj;
	}
	public static Object TransFromJSONToJava(Class<?> cls,Object obj)
	{
		return TransFromJSONToJava(utils.GetClassName(cls),obj);
	}
	public static Object TransFromJSONToJava(String clsName,Object obj)
	{
		if(obj==null)return null;
		Object eo=TransTojxEunm(clsName,obj);
		if(eo!=null)return eo;
		else if(clsName.compareTo("UUID")==0)
			return Trans.TransToUUID((String)obj);
		else if(clsName.compareTo("Date")==0)
			return Trans.TransToDate((Integer)obj);
		else
			return TransTo(clsName,obj);
	}
	
	public static ORMID TransToORMID(byte[] bs,int start)
	{
		if(bs==null||start<0||bs.length<start+4+16)return null;
		return new ORMID(TransToInteger(bs,start),TransToUUID(bs,start+4));
	}
	public static byte[] TransToByteArray(ORMID id)
	{
		if(id==null)return null;
		byte[] bs=new byte[4+16];
		TransToByteArray(bs,0,id.getTypeID());
		TransToByteArray(bs,4,id.getID());
		return bs;
	}
	public static void TransToByteArray(byte[] bs,int start,ORMID id)
	{
		if(bs==null||start<0||bs.length<start+8)return;
		TransToByteArray(bs,start+0,id.getTypeID());
		TransToByteArray(bs,start+4,id.getID());
	}
	public static byte[] TransToByteArray(UUID id)
	{
		if(id==null)return null;
		byte[] bs=new byte[16];
		TransToByteArray(bs,0,id);
		return bs;
	}
	public static void TransToByteArray(byte[] bs,int start,UUID id)
	{
		if(bs==null||start<0||bs.length<start+16)return;
		long lm=id.getMostSignificantBits();
		TransToByteArray(bs,start+8,lm);
		long ll=id.getLeastSignificantBits();
		TransToByteArray(bs,start,ll);
	}
	public static UUID TransToUUID(byte[] bs,int start)
	{
		if(bs==null||start<0||bs.length<start+16)return null;
		return new UUID(TransToLong(bs,start+8),TransToLong(bs,start));
	}
	public static String TransToString(UUID id)
	{
		if(id==null)return null;
		String s = id.toString(); 
        //去掉“-”符号 
        return s.substring(0,8)+s.substring(9,13)+s.substring(14,18)+s.substring(19,23)+s.substring(24); 
	}
	public static UUID TransToUUID(String str)
	{
		if(str==null)return null;
		String s=str.substring(0,8)+"-"+str.substring(8,12)+"-"+str.substring(12,16)+"-"+str.substring(16,20)+"-"+str.substring(20); 
		return UUID.fromString(s);
	}
	
	public static Object TransTo(Class<?> cls,Object value)
	{
		return TransTo(utils.GetClassName(cls),value);
	}
	public static Object TransTo(String clsName,Object value)
	{
		Object eo=TransTojxEunm(clsName,value);
		if(eo!=null)return eo;
		switch(clsName)
		{
		case "byte":
		case "Byte":
			return TransToByte(value);
		case "short":
		case "Short":
			return TransToShort(value);
		case "int":
		case "Integer":
			return TransToInteger(value);
		case "long":
		case "Long":
			return TransToLong(value);
		case "float":
		case "Float":
			return TransToFloat(value);
		case "double":
		case "Double":
			return TransToDouble(value);
		case "boolean":
		case "Boolean":
			return TransToBoolean(value);
		case "Date":
			return TransToDate(value);
		}
		return value;
	}

	public static Date TransToDate(Integer i)
	{
		if(i==null)return null;
		return new Date((long)i*1000);
	}
	public static Boolean TransToBoolean(Object value)
	{
		if(value==null)return false;
		try
		{
			return Boolean.parseBoolean(String.valueOf(value));
		}catch(Exception e){	return false;}
	}
	public static Double TransToDouble(Object value)
	{
		if(value==null)return 0D;
		try
		{
			return Double.parseDouble(String.valueOf(value));
		}catch(Exception e){	return 0D;}
	}
	public static Float TransToFloat(Object value)
	{
		if(value==null)return 0F;
		try
		{
		return Float.parseFloat(String.valueOf(value));
		}catch(Exception e){	return 0F;}
	}
	public static Long TransToLong(Object value)
	{
		if(value==null)return 0L;
		try
		{
		return Long.parseLong(String.valueOf(value));
		}catch(Exception e){	return 0L;}
	}
	public static Integer TransToInteger(Object value)
	{
		if(value==null)return 0;
		try
		{
		return Integer.parseInt(String.valueOf(value));
		}catch(Exception e){	return 0;}
	}
	public static Integer TransToInteger(Date d)
	{
		if(d==null)return 0;
		try
		{
		return (int) (((Date)d).getTime()/1000);
		}catch(Exception e){	return 0;}
	}
	public static Short TransToShort(Object value)
	{
		if(value==null)return 0;
		try
		{
		return Short.parseShort(String.valueOf(value));
		}catch(Exception e){	return 0;}
	}
	public static Byte TransToByte(Object value)
	{
		if(value==null)return 0;
		try
		{
		return Byte.parseByte(String.valueOf(value));
		}catch(Exception e){	return 0;}
	}
	public static Date TransToDate(Object str)
	{
		if(str==null)return null;
		try
		{
		 return DateTimeFormatter.parse(String.valueOf(str));
		}catch(Exception e){	return null;}
	}
	public static String TransToChinese(Date dt)
	{
		return TransToChinese_Date(dt)+" "+TransToChinese_Time(dt);
	}
	public static String TransToChinese_Date(Date dt)
	{
		Calendar c=Calendar.getInstance();
		c.setTime(dt);
		return c.get(Calendar.YEAR)+"年"+(c.get(Calendar.MONTH)+1)+"月"+c.get(Calendar.DATE)+"日";
	}
	public static String TransToChinese_Time(Date dt)
	{
		Calendar c=Calendar.getInstance();
		c.setTime(dt);
		return c.get(Calendar.HOUR)+"时"+c.get(Calendar.MINUTE)+"分"+c.get(Calendar.SECOND)+"秒";
	}
	
	public static String TransToChinese(int num)
	{
		String rs=null;
		int n1=num%100000000;
		int n2=num/100000000;
		if(n2>0)
		{
			rs=getChineseHundredMillion(n2)+"亿";
			if(n1<10000000)
				rs+="零";
			rs+=getChineseHundredMillion(n1);
		}
		else
			rs=getChineseHundredMillion(n1);
		return rs;
	}
	//亿以下数字
	private static String getChineseHundredMillion(int num)
	{
		String rs=null;
		int n=num%100000000;
		int n1=n%10000;
		String s1=getChineseMyria(n1);
		int n2=n/10000;
		String s2=getChineseMyria(n2);
		if(n2>0)
		{
			rs=s2+"万";
			if(n1<1000)
				rs+="零";
			rs+=s1;
		}
		else
			rs=s1;
		return rs;
	}
	//万以下数字
	private static String getChineseMyria(int num)
	{
		int n1=num%10;
		int n=num/10;
		int n2=n%10;
		n=n/10;
		int n3=n%10;
		n=n/10;
		int n4=n%10;
		boolean zero=false;
		String rs="";
		if(n4>0)
			rs=getChineseUnit(n4)+"千";
		if(n3>0)
			rs+=getChineseUnit(n3)+"百";
		else if(rs!="")
		{
			rs+="零";
			zero=true;
		}
		if(n2>0)
			rs+=getChineseUnit(n2)+"十";
		else if(rs!=""&&!zero)
		{
			rs+="零";
			zero=true;
		}
		if(n1>0)
			rs+=getChineseUnit(n1);
		if(rs=="") rs="零";
		return rs;
	}
	//最后一位数字
	private static String getChineseUnit(int num)
	{
		int nc=num%10;
		switch(nc)
		{
		case 0:
			return "零";
		case 1:
			return "一";
		case 2:
			return "二";
		case 3:
			return "三";
		case 4:
			return "四";
		case 5:
			return "五";
		case 6:
			return "六";
		case 7:
			return "七";
		case 8:
			return "八";
		case 9:
			return "九";
		}
		return "";
	}
	
	public static int TransToInteger(Enum<?> e)
	{
		return e.ordinal();
	}
	public static Integer TransToInteger(byte[] bs,int start)
	{
		if(bs==null)return 0;
		Integer value= 0;
		//由高位到低位
		for (int i = 0; i < 4; i++)
		{
			int shift= (4 - 1 - i) * 8;
			//往高位游
			value +=(bs[start+i] & 0xFF) << shift;
		}
		return value;
	}
	public static Long TransToLong(byte[] bs,int start)
	{
		if(bs==null)return 0L;
		Long value= 0L;
		//由高位到低位
		for (int i = 0; i < 8; i++)
		{
			int shift= (8 - 1 - i) * 8;
			//往高位游
			value +=((long)(bs[start+i] & 0xFF)) << shift;
		}
		return value;
	}
	/**
	 * 将num放到从start起的bs中，连续4个字节
	 * @param bs
	 * @param start
	 * @param num
	 */
	public static void TransToByteArray(byte[] bs,int start,int num)
	{
		bs[start+0] = (byte)((num >> 24) & 0xFF);
		bs[start+1] = (byte)((num >> 16) & 0xFF);
		bs[start+2] = (byte)((num >> 8) & 0xFF);
		bs[start+3] = (byte)(num & 0xFF);
	}
	public static byte[] TransToByteArray(int num)
	{
		byte[] bs=new byte[4];
		TransToByteArray(bs,0,num);
		return bs;
	}
	public static byte[] TransToByteArray(long num)
	{
		byte[] bs=new byte[8];
		TransToByteArray(bs,0,num);
		return bs;
	}
	public static void TransToByteArray(byte[] bs,int start,long num)
	{
		bs[start+0] = (byte)((num >> 56) & 0xFF);
		bs[start+1] = (byte)((num >> 48) & 0xFF);
		bs[start+2] = (byte)((num >> 40) & 0xFF);
		bs[start+3] = (byte)((num >> 32) & 0xFF);
		bs[start+4] = (byte)((num >> 24) & 0xFF);
		bs[start+5] = (byte)((num >> 16) & 0xFF);
		bs[start+6] = (byte)((num >> 8) & 0xFF);
		bs[start+7] = (byte)(num & 0xFF);
	}
	
	
	public static InstanceState TransToInstanceState(String str)
	{
		switch(str)
		{
		case "Waiting":
			return InstanceState.Waiting;
		case "Doing":
			return InstanceState.Doing;
		case "Closed":
			return InstanceState.Closed;
		case "Canceled":
			return InstanceState.Canceled;
		case "Paused":
			return InstanceState.Paused;		
		}
		return InstanceState.NoActive;
	}

	private static Map<String,IjxEnum> jxEnumTransor=new HashMap<String,IjxEnum>();
	public static Object TransTojxEunm(Class<?> cls,Object v)
	{
		IjxEnum e=jxEnumTransor.get(utils.GetClassName(cls));
		if(e!=null)
		{
			if(v==null)
				return e.TransToORMEnum(0);
			return e.TransToORMEnum((Integer) v);
		}
		return null;
	}	
	public static Object TransTojxEunm(String clsName,Object v)
	{
		IjxEnum e=jxEnumTransor.get(clsName);
		if(e!=null)
		{
			if(v==null)
				return e.TransToORMEnum(0);
			return e.TransToORMEnum((Integer) v);
		}
		return null;
	}
	public static void AddEunmType(IjxEnum e)
	{
		jxEnumTransor.put(utils.GetClassName(e.getClass()), e);
	}

}


