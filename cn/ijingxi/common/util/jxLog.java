
package cn.ijingxi.common.util;

import java.util.*;

import cn.ijingxi.common.app.jxSystem;
import cn.ijingxi.common.orm.*;

/**
 * 只在全局中有，
 * 
 * 统计分析在lanserver中每天晚上运行
 * 本机上只统计分析自己的
 * 
 * @author andrew
 *
 */
public class jxLog extends jxORMobj
{
	public static void Init() throws Exception{	InitClass(ORMType.jxLog.ordinal(),jxLog.class);}
	public static void CreateDB() throws Exception
	{
		CreateTableInDB(jxLog.class,null);
	}

	@ORM(Index=1)
	public UUID OwnerID;	
	
	@ORM(Index=2)
	public int TypeID;
	@ORM(Index=2)
	public int ID;
	
	@ORM
	public String Name;
	
	@ORM
	public String Descr;

	@ORM(Index=3)
	public Date LogTime;
	
	@ORM(Descr="json格式的附加信息")
	public String Info;


	/**
	 * 尚未保存，最终设置完毕要注意保存
	 * @param OwnerID
	 * @param TypeID
	 * @param ID
	 * @param Name
	 * @param Descr
	 * @return
	 * @throws Exception
	 */
	public static jxLog Log(UUID OwnerID,int TypeID,int ID,String Name,String Descr) throws Exception
	{
		jxLog log=(jxLog) jxLog.New(jxLog.class);
		log.OwnerID=OwnerID;
		log.TypeID=TypeID;
		log.ID=ID;
		log.Name=Name;
		log.Descr=Descr;
		log.LogTime=new Date();
		return log;
	}
	public void setInfo(String Purpose,Object value) throws Exception
	{
		setExtendValue("Info",Purpose,value);
	}
	
	public static jxMsg NewLogMsg(jxLog log,UUID Receiver) throws Exception
	{
		jxMsg msg=(jxMsg) jxMsg.New(jxMsg.class);
		msg.Sender=jxSystem.System.SystemUUID;
		msg.SenderID=ORMID.SystemID;
		msg.Receiver=Receiver;
		msg.ReceiverID=ORMID.SystemID;
		msg.MsgID=jxSystem.System.GetMsgID();
		msg.MsgType=jxMsgType.Log;
		msg.setMsg(log.ToJSONString());
		return msg;
	}
	public static jxLog GetFromMsg(jxMsg msg) throws Exception
	{
		return (jxLog) Trans.TransFromJSONToJava(jxLog.class, msg.getMsg());
	}
	
}