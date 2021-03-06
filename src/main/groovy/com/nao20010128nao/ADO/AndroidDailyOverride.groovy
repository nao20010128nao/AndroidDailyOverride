package com.nao20010128nao.ADO

import java.security.MessageDigest
import java.time.LocalDate

class AndroidDailyOverride {
    private static boolean executed
    private static boolean successful

    /**
     * Sets ANDROID_DAILY_OVERRIDE.
     * Only works with OpenJDK or Oracle's proprietary JDK.
     * */
    static void run(){
        if(executed){
            return
        }
        // most crazy way to modify env
        def privEnv = null
        try {
            /* for windows: only windows has theCaseInsensitiveEnvironment */
            privEnv = ('java.lang.ProcessEnvironment' as Class).getDeclaredField('theCaseInsensitiveEnvironment').with {
                accessible = true
                get(null)
            }
        } catch (Throwable e) {
            try {
                /* for *nix: use theEnvironment instead of theCaseInsensitiveEnvironment */
                def envTmp = ('java.lang.ProcessEnvironment' as Class).getDeclaredField('theEnvironment').with {
                    accessible = true
                    get(null)
                }
                privEnv = ('java.lang.ProcessEnvironment$StringEnvironment' as Class).declaredConstructors.find {
                    it.parameterTypes[0].name == Map.name
                }.with {
                    accessible = true
                    newInstance(envTmp)
                }
            } catch (Throwable e_) {
                successful=false
                return
            }
        }
        assert privEnv instanceof Map
        //com.android.build.gradle.internal.NonFinalPluginExpiry
        //java.lang.ProcessEnvironment
        def value = privEnv.ANDROID_DAILY_OVERRIDE = ({
            def now = LocalDate.now()
            def crypt = MessageDigest.getInstance("SHA-1")
            crypt.reset()
            crypt.update(
                String.format(
                    '%1$s:%2$s:%3$s',
                    now.year,
                    now.monthValue - 1,
                    now.dayOfMonth
                ).getBytes("utf8")
            )
            new BigInteger(1, crypt.digest()).toString(16)
        })()
        //check the env has intended value and non-null
        assert value && privEnv.ANDROID_DAILY_OVERRIDE &&
            System.getenv("ANDROID_DAILY_OVERRIDE") && value == System.getenv("ANDROID_DAILY_OVERRIDE")
        successful=true
        executed=true
    }

    static boolean getSuccessful(){successful}
}
