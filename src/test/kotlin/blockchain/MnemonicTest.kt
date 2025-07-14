package blockchain

import cash.z.ecc.android.bip39.Mnemonics
import cash.z.ecc.android.bip39.toSeed
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class MnemonicTest {

    @Test
    fun `test bip39 new seed from mnemonic`() {
        val mn = "legal winner thank year wave sausage worth useful legal winner thank yellow"
        val seed = Mnemonics.MnemonicCode(mn.toCharArray()).toSeed()
        println(seed.size)
        val expectedSeed = "878386efb78845b3355bd15ea4d39ef97d179cb712b77d5c12b6be415fffeffe5f377ba02bf3f8544ab800b955e51fbff09828f682052a20faa6addbbddfb096"
        assertEquals(expectedSeed, seed.joinToString("") { byte -> "%02x".format(byte) })
    }
}
