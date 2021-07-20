/*
 * Copyright (c) 2021, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package test.robot.javafx.stage;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.robot.Robot;
import javafx.stage.Stage;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import test.util.Util;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class WrongStageFocusWithApplicationModalityTest {
    private static Robot robot;
    private static Stage stage;
    private static CountDownLatch startupLatch = new CountDownLatch(3);
    private static CountDownLatch alertCloseLatch = new CountDownLatch(3);

    @BeforeClass
    public static void initFX() throws Exception {
        new Thread(() -> Application.launch(TestApp.class, (String[]) null)).start();
        waitForLatch(startupLatch, 10, "FX runtime failed to start.");
    }

    @Test(timeout = 15000)
    public void testWindowFocusByClosingAlerts() throws Exception {
        keyPress(KeyCode.ESCAPE);
        keyPress(KeyCode.ESCAPE);
        keyPress(KeyCode.ESCAPE);
        waitForLatch(alertCloseLatch, 10, "Alerts not closed, wrong focus");
    }

    @AfterClass
    public static void exit() {
        Platform.runLater(() -> {
            stage.hide();
        });
        Platform.exit();
    }

    private static void waitForLatch(CountDownLatch latch, int seconds, String msg) throws Exception {
        Assert.assertTrue("Timeout: " + msg, latch.await(seconds, TimeUnit.SECONDS));
    }

    private static void keyPress(KeyCode code) throws Exception {
        Util.runAndWait(() -> {
            robot.keyPress(code);
            robot.keyRelease(code);
            Util.sleep(50);
        });
    }

    public static class TestApp extends Application {
        @Override
        public void start(Stage primaryStage) {
            robot = new Robot();
            stage = primaryStage;

            BorderPane root = new BorderPane();
            stage.setScene(new Scene(root, 500, 500));
            stage.show();

            showAlert();
            showAlert();
            showAlert();
        }

        private void showAlert()
        {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.initOwner(stage);
            alert.setOnShown(event -> Platform.runLater(startupLatch::countDown));
            alert.setOnHidden(event -> Platform.runLater(alertCloseLatch::countDown));
            alert.show();
        }
    }
}
