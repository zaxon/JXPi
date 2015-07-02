
package cn.ijingxi.common.Process;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;


import cn.ijingxi.common.app.People;
import cn.ijingxi.common.app.PeopleInTs;
import cn.ijingxi.common.app.TopSpace;
import cn.ijingxi.common.app.jxSystem;
import cn.ijingxi.common.msg.MsgCenter;
import cn.ijingxi.common.msg.jxMsg;
import cn.ijingxi.common.orm.ContionLink;
import cn.ijingxi.common.orm.OPLink;
import cn.ijingxi.common.orm.ORM;
import cn.ijingxi.common.orm.ORMID;
import cn.ijingxi.common.orm.ORMType;
import cn.ijingxi.common.orm.jxJson;
import cn.ijingxi.common.orm.jxORMSM;
import cn.ijingxi.common.orm.ORM.KeyType;
import cn.ijingxi.common.util.*;

//是否自动执行，或输出情况下Auto为true则从所有出口中按顺序挑选第一个可以执行的节点进行触发
public class PN extends jxTask
{    
	public static String Node_Start = "开始";
	public static String Node_End = "结束";
	public static String Node_Accept = "同意";
	public static String Node_Reject = "拒绝";  
	
	public static ORMID GetORMID(UUID ID)
	{
		return new ORMID(ORMType.PN.ordinal(),ID);
	}

	@Override
	protected void Init_Create() throws Exception
	{
		ID=UUID.randomUUID();
		TaskType=jxTaskType.ProcessNodeInstance;
		State=InstanceState.Doing;
	}
	
	public static void Init() throws Exception
	{	
		InitClass(ORMType.PN.ordinal(),PN.class);
	}
	public static void CreateDB(TopSpace ts) throws Exception
	{
		CreateTableInDB(PN.class,ts);
	}
	/*
	@Override
	protected boolean CheckForMsgRegister() throws Exception
	{
		return true;
	}
	protected boolean DualEventMsg(jxMsg msg) throws Exception
	{
		IjxEnum event = msg.getEvent();
		if(event==null||!(event instanceof InstanceEvent))return false;
		TopSpace ts=msg.getTopSpace();
		if(ts==null)return false;
		PN from=(PN) jxORMobj.GetFromJSONString(msg.GetParam("Node"));
		String toname=msg.GetParam("To");
		switch((InstanceEvent)event)
		{
		case Touch:
			PN pn=getPN(ts,from.ParentID,toname);
			if(pn==null)
			{
				pn=(PN) PN.New(PN.class);
				pn.ParentOwnerID=msg.Receiver;
				pn.ParentID=from.ParentID;
				pn.ProcessID=from.ProcessID;
				pn.Parent=from.Parent;
				pn.Name=toname;
				pn.Insert(ts);
			}
			pn.Touch(ts, msg.getMsg());
				
			
			
			return true;
		default:
			break;
		}
		return false;
	
	
	
	}
	*/
	
	
	@ORM(keyType=KeyType.PrimaryKey)
	public UUID ID;

	@ORM(Index=1)
	public UUID ProcessID;
	public jxProcess getProcess(TopSpace ts) throws Exception
	{
		return (jxProcess) GetByID(jxProcess.class,ProcessID,ts);
	}
	
	@ORM(Descr="json格式的输入token,From,TokenNum")
	public String InputToken;

	public ORMID GetPIID() throws Exception
	{
		return new ORMID(ORMType.PI.ordinal(),ParentID);
	}


	//创建新对象时会被调用；从数据库读出时也会被调用，但会立刻被myInit覆盖
	PN()
	{
		State=InstanceState.Waiting;
	}
	void setParam(PeopleInTs Caller,UUID ProcessID,PI pi,String NodeName) throws Exception 
	{
		this.ParentOwnerID=jxSystem.SystemID;
		this.ParentID=pi.ID;
		this.ProcessID=ProcessID;
		this.Parent=pi.ToJSONString();
		this.Name=NodeName;
	}
	
	jxMsg GetEvnetMsg(TopSpace ts,UUID Receiver,ORMID ReceiverID,IjxEnum Event,String Msg,String FromName,String ExportName,String ToName) throws Exception
	{
		//消息一定是发给pi，但由于pi也有相同的事件，所有要加以区分
		ORMID pid=new ORMID(ORMType.PI.ordinal(),Trans.TransToUUID((String)GetParent("ID")));
		jxMsg msg=jxMsg.NewEventMsg(ts,getID(),Receiver,pid,Event,Msg);
		msg.SetParam("Node",this.ToJSONString());
		msg.SetParam("tsid",Trans.TransToString(ts.ID));
		setMsgParam(msg,FromName,ExportName,ToName);
		return msg;
	}

	static PN GetPNFromMsg(jxMsg msg) throws Exception
	{
		//msg里还有信息
		return (PN) GetFromJSON(jxJson.JsonToObject(msg.getMsg()));
	}
	
	void setMsgParam(jxMsg msg,String FromName,String ExportName,String ToName) throws Exception
	{
		//消息一定是发给pi，但由于pi也有相同的事件，所有要加以区分
		msg.SetParam("Purpose", "PN");
		msg.SetParam("NodeName", Name);
		msg.SetParam("ID", ID.toString());
		msg.SetParam("From",FromName);
		msg.SetParam("Export", ExportName);
		msg.SetParam("To", ToName);
		//TopSpace ts=(TopSpace) TopSpace.GetByID(TopSpace.class, Trans.TransToUUID(msg.GetParam("tsid")),null);
	}

	//第一个参数是节点，第二个参数是From节点名，第三个参数是出口名
    public void Touch(TopSpace ts,String Msg) throws Exception
    {
    	CallParam param=new CallParam(null,null,Msg);
    	param.addParam(this);
    	param.addParam(null);
    	param.addParam(null);
    	param.addParam(ts);
    	NodeSM.Happen(this, "State", InstanceEvent.Touch, param);
    	Update(null);
    }
    public void Close(TopSpace ts,String ExportName,String Msg) throws Exception
    {    	
    	CallParam param=new CallParam(null,null,Msg);
    	param.addParam(this);
    	param.addParam(null);
    	param.addParam(ExportName);
    	param.addParam(ts);
    	NodeSM.Happen(this, "State", InstanceEvent.Close, param);
    	Update(null);
    }
    /*
    public void Pause(String NodeName,IExecutor Caller,String Msg) throws Exception
    {    	
    	jxProcess p=PI.GetProcess(PIID);
    	IExecutor execer=p.getNode_RealExecer(NodeName);
    	if(execer==null)
    		//没有具体的执行者，可能是自动执行的系统节点
    		execer=PI.GetCteater(PIID);
		jxMsg msg=GetEvnetMsg(Caller,execer,InstanceEvent.Pause,Msg,null,null,Name);
		MsgCenter.Post(msg);
    }
    public void ReDo(IExecutor Caller,String Msg) throws Exception
    {
    	jxProcess p=PI.GetProcess(PIID);
    	IExecutor execer=p.getNode_RealExecer(Name);
    	if(execer==null)
    		//没有具体的执行者，可能是自动执行的系统节点，所以发给流程实例的创建者那里执行
    		execer=PI.GetCteater(PIID);
		jxMsg msg=GetEvnetMsg(Caller,execer,InstanceEvent.Trigger,Msg,null,null,Name);
		MsgCenter.Post(msg);
    }
	
	*/
	
	
	void HasInput(String From) throws Exception
	{
		if(From==null)return;
		int num=getInputToken(From);
		num++;
		setInputToken(From,num);
	}	
	void setInputToken(String From,Integer TokenNum) throws Exception
	{
		Map<String,String> ks=new HashMap<String,String>();
		ks.put("From", From);
		setExtendArrayValue(InputToken,ks,"TokenNum",TokenNum);
	}
	int getInputToken(String From) throws Exception
	{
		Map<String,String> ks=new HashMap<String,String>();
		ks.put("From", From);
		String v=getExtendArrayValue(InputToken,ks,"TokenNum");
		return Trans.TransToInteger(v);
	}
	boolean checkInputToken(TopSpace ts) throws Exception
	{
		jxProcess p=getProcess(ts);
		boolean allin=true;
		int num=0;
		List<String> fl=p.ListFrom(Name);
		Map<String,Integer> itl=new HashMap<String,Integer>();
		for(String f:fl)
		{
			num=getInputToken(f);
			if(num<=0)
			{
				allin=false;
				break;
			}
			itl.put(f, num);
		}
		if(allin)
			for(String f:fl)
			{
				num=itl.get(f);
				num--;
				setInputToken(f,num);
			}
		return allin;		
	}
	
	
	static jxORMSM<InstanceState,InstanceEvent> NodeSM=null;
	static
	{
		NodeSM=new jxORMSM<InstanceState,InstanceEvent>();
		//初始化节点的状态转换
		NodeSM.AddTrans(InstanceState.Waiting, InstanceEvent.Touch, InstanceState.Waiting, new CheckNodeInput());
		NodeSM.AddTrans(InstanceState.Waiting, InstanceEvent.Trigger, InstanceState.Doing, new NodeDual());
		NodeSM.AddTrans(InstanceState.Doing, InstanceEvent.Close, InstanceState.Closed, new NodeClose());
		//NodeSM.AddTrans(InstanceState.Doing, InstanceEvent.Pause, InstanceState.Paused, new NodePause());
		//NodeSM.AddTrans(InstanceState.Paused, InstanceEvent.Trigger, InstanceState.Doing, new NodeDual());
	}
	
}


class CheckNodeInput implements IDoSomething
{
	@Override
	public void Do(CallParam param) throws Exception 
	{
		//第一个参数是节点，第二个参数是From节点名，第三个参数是出口名，第四个参数是TopSpaceID
		PN node= (PN)param.getParam();
		People caller=(People) param.getCaller();
		String fromname=(String) param.getParam();
		String exportname=(String) param.getParam();
		TopSpace ts=(TopSpace) param.getParam();
		jxProcess process=node.getProcess(ts);
		if(process.getNode_InputType(node.Name))
			//就本功能来说其实只需要锁住进程的NodeInputToken，但考虑到进程本身也有可能同时进行动作，所以整体锁住
	        synchronized (node)
	        {
	        	node.HasInput(fromname);
	        	if(node.checkInputToken(ts))
    				//token到齐，发出一个触发事件
    				PN.NodeSM.Happen(node, "State", InstanceEvent.Trigger, param);
    		}
	}
}

class NodeDual implements IDoSomething
{
	@Override
	public void Do(CallParam param) throws Exception 
	{
		//第一个参数是节点，第二个参数是From节点名，第三个参数是出口名
		PN node= (PN)param.getParam();
		PeopleInTs caller=(PeopleInTs) param.getCaller();
		String fromname=(String) param.getParam();
		String exportname=(String) param.getParam();
		TopSpace ts=(TopSpace) param.getParam();
		jxProcess process=node.getProcess(ts);
		IExecutor execer=process.getNode_RealExecer(ts,node.Name);
		if(node.Name==PN.Node_End)
		{			
			jxMsg msg=jxMsg.NewEventMsg(ts,node.getID(),node.ParentOwnerID,PI.GetORMID(node.ParentID),InstanceEvent.Close,null);
			msg.SetParam("NodeName", node.Name);
			MsgCenter.Post(msg);
		}
		else if(node.getProcess(ts).getNode_OutputType(node.Name)||node.getProcess(ts).getNode_Auto(node.Name))
			//如果是与输出则相当于自动执行，不再等待Execer的动作	
			PN.NodeSM.Happen(node, "State", InstanceEvent.Close, param);
		else if(execer!=null)
		{
			/*
			String m=String.format("<a>流程：%s的任务%s已流转到您，请尽快处理</a>", node.GetParent("Name"),node.Name);
			jxMsg msg=jxMsg.NewRichMsg(ts,node.getID(),execer.getUniqueID(),execer.getID(),m);
			node.setMsgParam(msg,fromname,exportname,node.Name);
			MsgCenter.Post(msg);
			*/
		}
	}
}

class NodeClose implements IDoSomething
{
	@Override
	public void Do(CallParam param) throws Exception
	{
		//第一个参数是节点，第二个参数是From节点名，第三个参数是出口名
		PN node= (PN)param.getParam();
		PeopleInTs caller=(PeopleInTs) param.getCaller();
		String fromname=(String) param.getParam();
		String exportname=(String) param.getParam();
		TopSpace ts=(TopSpace) param.getParam();
		jxProcess process=node.getProcess(ts);
		if(process.getNode_OutputType(node.Name))
		{
			List<String> el = process.ListExport(node.Name);
			for(String e:el)
			{
				String tn=process.getTrans_To(node.Name, e);
				UUID epid=null;
				ORMID eid=null;
				PeopleInTs execer=process.getNode_RealExecer(ts,tn);
		    	if(execer==null)
		    	{
		    		//没有具体的执行者，可能是自动执行的系统节点，所以发给流程实例的创建者那里执行
		    		epid=node.ParentOwnerID;
		    		eid=PI.GetORMID(node.ParentID);
		    	}
		    	else
		    	{
		    		//下一节点的执行者，可能还没有创建这个节点，所以消息是发给进程的，如果没创建则创建，然后再转交
		    		epid=execer.ID;
		    		eid=jxProcess.GetORMID(node.ProcessID);
		    	}
				jxMsg msg=node.GetEvnetMsg(ts,epid,eid,InstanceEvent.Touch,null,node.Name,e,tn);
				MsgCenter.Post(msg);
			}
		}
		else if(process.getNode_Auto(node.Name))
		{
			PI pi=null;
			String ename=null;
			boolean abe=process.getNode_AutoByExecer(node.Name);
			List<String> el = process.ListExport(node.Name);			
			for(String e:el)
			{
				if(abe)
				{
					String tn=process.getTrans_To(node.Name, e);
					PeopleInTs execer=process.getNode_RealExecer(ts,tn);
			    	if(execer!=null)
			    	{
			    		ename=e;
			    		break;
			    	}
				}
				else
				{
					//后继分支选择
					ContionLink cl=process.getTranContion(node.Name, e);
					if(cl!=null&&cl.Judge(pi))
					{
			    		ename=e;
			    		break;
					}
					//
					//执行都会发回pi来执行
					//
					//操作：目前只有设置结果
					OPLink ol=process.getNodeOP(node.Name);
					if(ol!=null)
					{
						jxMsg msg=node.GetEvnetMsg(ts,node.ParentOwnerID,PI.GetORMID(node.ParentID),InstanceEvent.SetResult,null,node.Name,null,null);
						msg.SetParam("Result", ol.Exec(pi));
						MsgCenter.Post(msg);
					}
				}
			}
			if(ename==null)
				throw new Exception(String.format("流程%s的节点%s自动执行，但未找到相应的执行分支！！",process.Name,node.Name));
			String tn=process.getTrans_To(node.Name, ename);
			UUID epid=null;
			ORMID eid=null;
			PeopleInTs execer=process.getNode_RealExecer(ts,tn);
	    	if(execer==null)
	    	{
	    		//没有具体的执行者，可能是自动执行的系统节点，所以发给流程实例的创建者那里执行
	    		epid=node.ParentOwnerID;
	    		eid=PI.GetORMID(node.ParentID);
	    	}
	    	else
	    	{
	    		//下一节点的执行者，可能还没有创建这个节点，所以消息是发给进程的，如果没创建则创建，然后再转交
	    		epid=execer.ID;
	    		eid=jxProcess.GetORMID(node.ProcessID);
	    	}
			jxMsg msg=node.GetEvnetMsg(ts,epid,eid,InstanceEvent.Touch,null,node.Name,ename,tn);
			MsgCenter.Post(msg);
		}
		else
		{
			//param.Trans是用户指定的
			String tn=process.getTrans_To(node.Name, exportname);
			PeopleInTs execer=process.getNode_RealExecer(ts, tn);
			UUID epid=null;
			ORMID eid=null;
	    	if(execer==null)
	    	{
	    		//没有具体的执行者，可能是自动执行的系统节点，所以发给流程实例的创建者那里执行
	    		epid=node.ParentOwnerID;
	    		eid=PI.GetORMID(node.ParentID);
	    	}
	    	else
	    	{
	    		//下一节点的执行者，可能还没有创建这个节点，所以消息是发给进程的，如果没创建则创建，然后再转交
	    		epid=execer.ID;
	    		eid=jxProcess.GetORMID(node.ProcessID);
	    	}
			jxMsg msg=node.GetEvnetMsg(ts,epid,eid,InstanceEvent.Touch,null,node.Name,exportname,tn);
			MsgCenter.Post(msg);
		}
	}
}




