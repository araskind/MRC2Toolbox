/*******************************************************************************
 *
 * (C) Copyright 2018-2020 MRC2 (http://mrc2.umich.edu).
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
 *
 * Contributors:
 * Alexander Raskind (araskind@med.umich.edu)
 *
 ******************************************************************************/

package edu.umich.med.mrc2.datoolbox.gui.utils;

import bibliothek.gui.DockStation;
import bibliothek.gui.Dockable;
import bibliothek.gui.dock.common.intern.DefaultCDockable;
import bibliothek.gui.dock.station.support.PlaceholderStrategy;
import bibliothek.gui.dock.station.support.PlaceholderStrategyListener;
import bibliothek.util.Path;

public class DockPlaceholderStrategy implements PlaceholderStrategy {
    public void addListener( PlaceholderStrategyListener listener ){
        // ignore
    }

    public Path getPlaceholderFor( Dockable dockable ){
        /* The placeholder for a ColorDockable is the unique identifier used
         * in our DockFrontend */
        if( dockable instanceof DefaultCDockable ){
            return new Path( ((DefaultCDockable)dockable).getTitleText() );
        }
        else{
            return null;
        }
    }

    public void install( DockStation station ){
        // ignore
    }

    public boolean isValidPlaceholder( Path placeholder ){
        /* Any placeholder is valid, we do not care about old placeholders that 
         * are no longer used. */
        return true;
    }

    public void removeListener( PlaceholderStrategyListener listener ){
        // ignore
    }

    public void uninstall( DockStation station ){
        // ignore
    }
}
