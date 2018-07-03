package airsys.controller;

import airsys.Configure;
import airsys.model.DataChangedListener;
import airsys.model.conditioner;
import airsys.org.json.JSONObject;
import airsys.service.DataSender;
import airsys.service.ServerListener;
import airsys.service.TcpServer;
import airsys.view.OverViewPanel;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.net.InetAddress;

public class ViewController {
    private DataSender sender;
    private OverViewPanel view;
    private conditioner model;
    private InetAddress remoteAddr;
    private int port=Configure.REMOTE_PORT;
    private boolean isOn=false;
    private Timer dataTimer;//for send status to the server
    private Thread serv;
    private boolean isFirst=true;

    public ViewController(OverViewPanel view, conditioner model1) throws IOException {
        this.view=view;
        this.model=model1;
        try {
            sender=new DataSender();
        } catch (IOException e) {
            e.printStackTrace();
        }
        remoteAddr=InetAddress.getByName(Configure.REMOTE_IP);
        int tick = Configure.DEFAULT_TICK;
        dataTimer = new Timer(tick, e -> {
            try {//每隔1秒传送房间状态
                sender.sendStatus(remoteAddr, port,model.getRoom(), model.getCurrentTemp());
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        });
        serv=new Thread(new TcpServer(sender.getServerSocket(),new ServerListener() {
            @Override
            public void onReceive(String msg) {
                //{"switch":1/0,"temperature":25.80,"wind":1,"cost":2.00}
                JSONObject jsonObject=new JSONObject(msg);
                int _switch=jsonObject.getInt("switch");
                if (_switch==0){
                    model.set_switch(0);//关机
                    view.bootButton.setText("开机");
                    serv.suspend();//挂起
                    dataTimer.stop();
                    view.disableBtn();
                    isOn=false;
                }else{
                    float targetTemp=jsonObject.getFloat("temperature");
                    int wind=jsonObject.getInt("wind");
                    float cost=jsonObject.getFloat("cost");
                    model.setCurrentPay(cost);
                    if (wind> conditioner.pending){
                        model.set_switch(1);
                        model.addTempChangeDaemon(true);
                    }else{//wind=0 开始回温，停止发送状态启动回温后再开启
                        dataTimer.stop();
                        model.addTempChangeDaemon(false);//设置回温
                        dataTimer.start();
                    }
                    model.setTargetTemp(targetTemp);
                    model.setWind(wind);
                }
            }

            @Override
            public void onException(Exception e) {
                //TODO
            }
        }));
        setupEvent();
    }

    private void setupEvent() {

        view.bootButton.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!isOn){
                    try {
                        sender.connect(remoteAddr,port,model.getRoom());
                        sender.request(remoteAddr,port,model.getRoom(),1,model.getTargetTemp(),model.mid);
                        dataTimer.start();
                        view.initStatus();
                        view.setCurrTemp((int) model.getCurrentTemp());
                        view.setTargetTemp((int) model.getTargetTemp());
                        view.setPayment(0.0f);
                        view.bootButton.setText("关机");
                        if (isFirst){
                            serv.start();
                            isFirst=false;
                        }else
                            serv.resume();
                        model.addDataChangedListener(new DataChangedListener() {
                            @Override
                            public void temperatureChanged(float temp) {
                                view.setCurrTemp((int) temp);
                            }

                            @Override
                            public void paymentChanged(float pay) {
                                view.setPayment(pay);
                            }

                            @Override
                            public void windChanged(int wind) {
                                view.windChange(wind);
                            }

                            @Override
                            public void workModeChanged(int workMode) {
                                view.changeWorkMode();
                            }

                            @Override
                            public void onException(Exception e) {
                                e.getStackTrace();
                            }
                        });
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                    isOn=true;
                }else{
                    try {
                        sender.disconnect(remoteAddr,Configure.REMOTE_PORT,model.getRoom());
                        sender.request(remoteAddr,port,model.getRoom(),0,model.getTargetTemp(),model.getWind());
                        model.set_switch(0);
                        view.bootButton.setText("开机");
                        serv.suspend();
                        dataTimer.stop();
                        view.disableBtn();
                        isOn=false;
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        });

        view.windButton.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int wind = model.getWind();
                if(wind == conditioner.high) {
                    wind = conditioner.pending;
                } else {
                    wind++;
                }
                sender.request(remoteAddr, port, model.getRoom(),model.get_switch(),model.getTargetTemp(), wind);
                view.windChange(wind);
            }
        });

        view.upButton.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int temp= (int) model.getTargetTemp();
                System.out.println("target: "+temp);
                if (model.setTargetTemp(++temp)){
                    view.setTargetTemp(temp);
                    sender.request(remoteAddr,port,model.getRoom(),model.get_switch(),temp,model.getWind());
                }
            }
        });

        view.downButton.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int temp= (int) model.getTargetTemp();
                System.out.println("target: "+temp);
                if (model.setTargetTemp(--temp)){
                    view.setTargetTemp(temp);
                    sender.request(remoteAddr,port,model.getRoom(),model.get_switch(),temp,model.getWind());
                }
            }
        });
    }
}
