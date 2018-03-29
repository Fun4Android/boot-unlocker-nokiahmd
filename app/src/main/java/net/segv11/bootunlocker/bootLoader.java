/******************************************************************************
 * Copyright 2018 Calyx Hikari                                                *
 *                                                                            *
 *   Licensed under the Apache License, Version 2.0 (the "License");          *
 *   you may not use this file except in compliance with the License.         *
 *   You may obtain a copy of the License at                                  *
 *                                                                            *
 *       http://www.apache.org/licenses/LICENSE-2.0                           *
 *                                                                            *
 *   Unless required by applicable law or agreed to in writing, software      *
 *   distributed under the License is distributed on an "AS IS" BASIS,        *
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. *
 *   See the License for the specific language governing permissions and      *
 *   limitations under the License.                                           *
 ******************************************************************************/

package net.segv11.bootunlocker;

import android.util.Log;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * @description manages the interface to the bootloader for locking and unlocking
 */
public class bootLoader {
    /**
     * constants describing bootloader state
     */
    public static final int BL_UNSUPPORTED_DEVICE = -2;
    public static final int BL_UNKNOWN = -1;
    public static final int BL_UNLOCKED = 0;
    public static final int BL_LOCKED = 1;
    public static final int BL_TAMPERED_UNLOCKED = 2;
    public static final int BL_TAMPERED_LOCKED = 3;

    // how long to wait after calling su to update param
    // before we update the UI:
    public static final long delayAfterChange = 200;    // 200ms
    private static final long launchDelay = 30;            // 30ms

    /**
     * For logging
     */
    private static final String TAG = "net.segv11.bootloader";

    /**
     * checks if we know how to lock/unlock the bootloader on this device
     */
    public static bootLoader makeBootLoader() {
        Log.v(TAG, "DEVICE = " + android.os.Build.DEVICE);
        if (android.os.Build.DEVICE.equals("D1C")) {
            return new bootLoader_nokia6();
        } else if (android.os.Build.DEVICE.equals("PLE")) {
            return new bootLoader_nokia6();
        } else if (android.os.Build.DEVICE.equals("D1A")) {
            return new bootLoader_nokia5();
        } else if (android.os.Build.DEVICE.equals("ND1")) {
            return new bootLoader_nokia5();
        } else {
            return null;
        }
        // TODO: Should we check android.os.Build.BOOTLOADER ?
    }

    /**
     * Does this bootloader support a tamper flag?
     */
    public boolean hasTamperFlag() {
        // We override this in relevant subclasses
        return false;
    }

    /**
     * Locks or unlocks the bootloader
     */
    public void setLockState(boolean newState) throws IOException {
        // We override this in subclasses
        return;
    }

    /**
     * Sets or clears the tamper flag
     */
    public void setTamperFlag(boolean newState) throws IOException {
        // We override this in relevant subclasses
        return;
    }

    /**
     * Finds out  if the bootloader is unlocked and if the tamper flag is set
     */
    public int getBootLoaderState() {
        // We override this in subclasses
        return BL_UNKNOWN;
    }

    /**
     * Low-level code for pushing a write command through SU
     */
    public void superUserCommandWithDataByte(String theCommand, int dataByte) throws IOException {
        Process p = Runtime.getRuntime().exec("su");
        DataOutputStream w = new DataOutputStream(p.getOutputStream());
        // BufferedReader r = new BufferedReader(new InputStreamReader(process.getInputStream()));
        w.writeBytes(theCommand + dataByte + "\n");    // dataByte here is just for logging
        w.flush();

        // Wait for the command to launch
        try {
            Thread.sleep(launchDelay);
        } catch (InterruptedException e) {
            // Should not happen; if it does, we just keep going.
            e.printStackTrace();
        }

        w.writeByte(dataByte);
        w.flush();
        w.close();
    }

    /**
     * Low-level code for pushing a query command through SU
     */
    public int superUserCommandWithByteResult(String theCommand) throws IOException {
        Process p = Runtime.getRuntime().exec("su");
        DataOutputStream w = new DataOutputStream(p.getOutputStream());
        DataInputStream r = new DataInputStream(p.getInputStream());

        w.writeBytes(theCommand + "\n");
        w.flush();
        int resultByte = r.readByte();
        // w.writeBytes("exit\n");
        // w.flush();
        w.close();
        return resultByte;
    }

}
