/*
 * Copyright (c) 2021 Stephan M. February (stephan@werkswinkel.com)
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

import org.twostack.bitcoin4j.crypto.*;
import org.twostack.bitcoin4j.exception.MnemonicException;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.List;
import java.util.stream.Collectors;


import static org.twostack.bitcoin4j.Utils.WHITESPACE_SPLITTER;

public class SimpleWallet {

    String generateSeedPhrase() throws IOException, MnemonicException.MnemonicLengthException {

        //32 bytes of randomness resulting in 24 seed words
        byte[] entropy = new SecureRandom().generateSeed(32);
        List<String> phrase = new MnemonicCode().toMnemonic(entropy);

        return phrase.stream().collect(Collectors.joining(" "));
    }

    DeterministicKey fromSeed(String seedPhrase) throws IOException, MnemonicException {
        List<String> words = WHITESPACE_SPLITTER.splitToList(seedPhrase);
        MnemonicCode mc = new MnemonicCode();

        mc.check(words);

        byte[] seedBytes = MnemonicCode.toSeed(words, "");

        DeterministicKey dk = HDKeyDerivation.createMasterPrivateKey(seedBytes);
        return dk;

    }

    public static void main(String[] args) {

        SimpleWallet wallet = new SimpleWallet();

        try {
            String seedPhrase = wallet.generateSeedPhrase();
            DeterministicKey rootKey = wallet.fromSeed(seedPhrase);

            //derive a new hardened keypair from our Master Key
            HDPath path = HDPath.parsePath("m/44H/0H/0H/182");

            //create a deterministic hierarchy in-memory to manage our keys
            DeterministicHierarchy dh = new DeterministicHierarchy(rootKey);

            //obtain the key at specific node in our hierarchy
            DeterministicKey dk = dh.get(path.subList(0, path.size()), true, true );

            //dump the paths to stdout to verify
            System.out.println(dk.getPathAsString());
            System.out.println(path.toString());


        } catch (IOException e) {
            System.out.println("Failed to load dictionary.Aborting. : " + e.getMessage());
        } catch (MnemonicException e) {
            System.out.println("Seed phrase not decoded.Aborting.: " + e.getMessage());
        }


    }
}
