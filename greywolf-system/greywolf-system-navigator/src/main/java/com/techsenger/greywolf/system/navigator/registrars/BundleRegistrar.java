/*
 * Copyright 2026 Pavel Castornii.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.techsenger.greywolf.system.navigator.registrars;

import com.techsenger.greywolf.core.GreyWolf;
import com.techsenger.greywolf.system.navigator.actions.NavigatorActions;
import com.techsenger.shellfx.base.registrar.AbstractRegistrar;
import com.techsenger.shellfx.core.registries.BundleRegistry;

/**
 * Registers the bundle with the texts of the actions of the Navigator plugin.
 *
 * @author Pavel Castornii
 */
public class BundleRegistrar extends AbstractRegistrar<BundleRegistry> {

    public BundleRegistrar(GreyWolf greyWolf) {
        super(greyWolf.getBundleRegistry());
    }

    @Override
    public void register() {
        addRegistration(getRegistry().register(NavigatorActions.Bundle.NAVIGATOR,
                NavigatorActions.loadBundle()));
    }
}
