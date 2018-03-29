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

import java.io.IOException;

/**
 * @description device-specific bootloader code for Nexus 10 tablets
 */
public class bootLoader_nokia5 extends bootLoader {
    /**
     * For logging
     */
    private static final String TAG = "net.segv11.bootLoader_nokia5";

    /**
     * Private constants for working with the lock state in the param partition
     */
    private static final String queryCommand =
            "dd ibs=1 count=1 skip=12545 if=/dev/block/bootdevice/by-name/deviceinfo  # query ";
    private static final String writeCommand =
            "dd obs=1 count=1 seek=12545 of=/dev/block/bootdevice/by-name/deviceinfo # write ";

    /**
     * Locks or unlocks the bootloader
     */
    @Override
    public void setLockState(boolean newState) throws IOException {
        int outByte;
        if (newState) {
            outByte = 0;
            Log.i(TAG, "Deacquiring Unlock Capability by sending " + outByte + " to " + writeCommand);
        } else {
            outByte = 255;
            Log.i(TAG, "Acquiring Unlock Capability by sending " + outByte + " to " + writeCommand);
        }

        superUserCommandWithDataByte(writeCommand, outByte);
    }


    /**
     * Finds out (from the param partition) if the bootloader is unlocked
     */
    @Override
    public int getBootLoaderState() {
        try {
            Log.v(TAG, "Getting bootloader state with " + queryCommand);

            int lockResult = superUserCommandWithByteResult(queryCommand);

            Log.v(TAG, "Got lock value " + lockResult);
            if (lockResult == 0) {
                return BL_LOCKED;
            } else {
                return BL_UNLOCKED;
            }
        } catch (IOException e) {
            Log.v(TAG, "Caught IOException while querying: " + e);
            return BL_UNKNOWN;
        }
    }


}
