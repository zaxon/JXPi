package cn.ijingxi.common.system;

import cn.ijingxi.common.util.Trans;
import cn.ijingxi.common.util.jxLog;
import cn.ijingxi.common.util.utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by andrew on 16-1-30.
 */
public class shell {

    private static Map<Integer,Process> processMap=new HashMap<>();

    /**
     * 如果有参数需事先填好
     *
     * @param cmd
     * @return
     */
    public static int exec(String cmd,String key) {
        return exec(cmd,key, 0);
    }
    public static int exec(String cmd,String key,int delaySecond) {
        jxLog.logger.debug("exec: " + cmd);
        Process process = null;
        try {
            //String fnstdout=LogFile+"_out.log";
            //String fnstderr=LogFile+"_err.log";
            //File fileout=new File(fnstdout);
            //if(!fileout.exists())
            //    fileout.createNewFile();
            //File fileerr=new File(fnstderr);
            //if(!fileerr.exists())
            //    fileerr.createNewFile();
            //FileOutputStream fo=new FileOutputStream(fileout);
            //FileOutputStream fe=new FileOutputStream(fileerr);
            String[] cmds = {"/bin/sh", "-c", cmd};
            process = Runtime.getRuntime().exec(cmds);
            //InputStream stdout = process.getInputStream();
            //InputStream stderr = process.getErrorStream();
            /*
            jxTimer.asyncRun(param -> {
                byte buffer[]=new byte[1024];
                int num=stdout.read(buffer);
                while(num!=-1){
                    fo.write(buffer,0,num);
                    num=stdout.read(buffer);
                }
                stdout.close();
                fo.close();
            },null);
            jxTimer.asyncRun(param -> {
                byte buffer[]=new byte[1024];
                int num=stderr.read(buffer);
                while(num!=-1){
                    fe.write(buffer,0,num);
                    num=stderr.read(buffer);
                }
                stderr.close();
                fe.close();
            },null);
            */
            if (delaySecond > 0)
                Thread.sleep(delaySecond * 1000);
            int pid = getProcessID(cmd, key, 4);
            if (pid > 0)
                processMap.put(pid, process);
            return pid;
        } catch (Exception e) {
            jxLog.error(e);
        }
        return 0;
    }

    public static void kill(int processID){
        try {
            Runtime.getRuntime().exec("kill -9 "+processID);
            //等待程序自行结束
            Thread.sleep(1000);
            Process p = processMap.remove(processID);
            if(p!=null)
                p.destroy();
        } catch (Exception e) {
            jxLog.error(e);
        }
    }

    /**
     * 由于可能同时运行多个同样的命令，所以可以送入识别关键字作为区分
     * @param cmd
     * @param key
     * @param colIndex
     * @return
     */
    public static int getProcessID(String cmd,String key,int colIndex){
        try {
            String[] css=utils.StringSplit(cmd," ");

            if(colIndex>=css.length)
                return 0;


            String line;
            String regstr=" ps -ax ";
            if(key!=null)
                regstr+="| grep "+css[0]+" | grep "+key;
            else
                regstr+="| grep '"+cmd+"' ";

            jxLog.logger.debug("ps cmd:"+regstr);

            String[] cmds = {"/bin/sh","-c",regstr};
            //String sc="/bin/sh -c ps | grep "+cmd+"";
            //jxLog.logger.debug("cmds:"+cmds);
            Process p = Runtime.getRuntime().exec(cmds);
            //for (int i = 0; i < css.length; i++) {
            //    jxLog.logger.debug(String.format("ss[%d]:%s", i, css[i]));
            //}

            jxLog.logger.debug("getProcessID cmd: "+css[0]);
            BufferedReader input = new BufferedReader
                    (new InputStreamReader(p.getInputStream()));
            while ((line = input.readLine()) != null) {
                jxLog.logger.debug("ps result: " + line);
                if (!line.trim().equals("")) {
                    String[] ss = utils.StringSplit(line, " ");

                    //for (int i = 0; i < ss.length; i++) {
                    //    jxLog.logger.debug(String.format("ss[%d]:%s", i, ss[i]));
                    //}


                    // Pid is after the 1st ", thus it's argument 3 after splitting
                    String pid = ss[0];
                    //jxLog.logger.debug("ProcessID: "+pid);
                    String pname = ss[colIndex];
                    //jxLog.logger.debug("ProcessName: "+pname);
                    if (pname.compareTo(css[0]) == 0)
                        return Trans.TransToInteger(pid);
                }
            }
            input.close();
        } catch (Exception e) {
            jxLog.error(e);
        }
        return 0;
    }
}