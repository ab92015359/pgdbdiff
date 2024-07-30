package com.vernon.pgdatadiff.core;

import com.vernon.pgdatadiff.model.EchoObject;
import com.vernon.pgdatadiff.utils.FileUtil;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Vernon Chen
 * @time 2024年7月27日 下午7:13:14
 */
@Slf4j
public class DataWriter implements Runnable {

    private Long lastTime = System.currentTimeMillis();

    @Override
    public void run() {
        EchoObject eo;
        while (true) {
            eo = DBDiffContext.echoQueue.poll();
            if (eo != null) {
                FileUtil.echo(eo.getFilePath(), eo.getContent());
            }

            if (DBDiffContext.isFinished && DBDiffContext.echoQueue.size() == 0) {
                log.info("========== Finish to process all of diff set ==========");
                System.exit(9);
            } else if (DBDiffContext.echoQueue.size() == 0) {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            long endTime = System.currentTimeMillis();
            long executionTime = endTime - lastTime;
            if (executionTime > 2000) {
                log.debug(String.format("data writer is working and queue have %s item.", DBDiffContext.echoQueue.size()));
                lastTime = endTime;
            }
        }
    }
}
