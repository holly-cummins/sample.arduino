/**
 * (C) Copyright IBM Corporation 2014.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.ibm.ws.sample.arduino;

/**
 * Callbacks enable server-side application code to run when particular
 * conditions or events occur on the Arduino microcontroller.
 * 
 * @see Arduino#digitalCallback, Arduino#analogCallback, Arduino#functionCallback
 */
public interface Callback {

    /**
     * Called when the condition of the Callback is triggered
     * 
     * @param value  the value that caused the callback to be triggered
     */
    void triggered(int value);

    /**
     * Called when the condition of the Callback is reset
     * 
     * @param value  the value that caused the callback to be reset
     */
    void reset(int value);
}
