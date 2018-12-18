/*
 * Copyright © 2018 by Sathish Babu Rathinavel (sathishbabur@hotmail.com)
 *
 * All rights reserved. No part of this publication may be reproduced, distributed, or transmitted in any form or by any means, including photocopying, recording, or other electronic or mechanical methods, without the prior written permission of the publisher, except in the case of brief quotations embodied in critical reviews and certain other noncommercial uses permitted by copyright law. For permission requests, write to the publisher, addressed “Attention: Permissions Coordinator,” at the address above.
 *
 * ARA Software
 * No. 54, Pratish Street,
 * V.G.N.Shanthi Nagar,
 * Ambattur, Chennai - 600 053.
 * Tamil Nadu, India.
 *  +91 9940 042 846
 *   044 4203 2099
 *   info@arasoftwares.com
 */

package com.arasoftware.call_recorder_demo.services;

import android.app.admin.DeviceAdminReceiver;
import android.content.Context;
import android.content.Intent;

public class DeviceAdminManager extends android.app.admin.DeviceAdminReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
    }

    public void onEnabled(Context context, Intent intent) {
    }


    public void onDisabled(Context context, Intent intent) {
    }
}