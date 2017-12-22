/*
 *    Copyright (C) 2017 Alexander Söderberg
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package xyz.kvantum.addon;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.UUID;

/**
 * Abstract class used to declare an addon
 */
@SuppressWarnings( "WeakerAccess" )
@EqualsAndHashCode
@ToString
public abstract class AddOn
{

    @Getter
    @Setter( AccessLevel.PACKAGE )
    private boolean enabled;

    @Getter
    @Setter( AccessLevel.PACKAGE )
    private AddOnClassLoader classLoader;

    @Getter
    @Setter( AccessLevel.PACKAGE )
    private String name;

    @Getter
    private final UUID uuid = UUID.randomUUID();

    void enable()
    {
        if ( this.isEnabled() )
        {
            throw new IllegalStateException( "Cannot enable the addon when it's already enabled" );
        }
        this.onEnable();
        this.setEnabled( true );
    }

    void disable()
    {
        if ( !this.isEnabled() )
        {
            throw new IllegalStateException( "Cannot disable the addon when it isn't enabled" );
        }
        this.onDisable();
        this.setEnabled( false );
    }

    /**
     * Called when the addon is enabled
     */
    protected abstract void onEnable();

    /**
     * Called when the addon is disabled
     */
    protected abstract void onDisable();
}
