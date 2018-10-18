/*
 * MeshIneBits is a Java software to disintegrate a 3d mesh (model in .stl)
 * into a network of standard parts (called "Bits").
 *
 * Copyright (C) 2016  Thibault Cassard & Nicolas Gouju.
 * Copyright (C) 2017-2018  TRAN Quoc Nhat Han.
 * Copyright (C) 2018 Vallon BENJAMIN.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package meshIneBits.gui.utilities;

import meshIneBits.config.PatternConfig;
import meshIneBits.config.patternParameter.PatternParameter;

/**
 * Contains specialized parameters for the chosen pattern
 */
public class PatternParametersContainer extends OptionsContainer {

    /**
     *
     */
    private static final long serialVersionUID = -5486094986597798629L;

    private GalleryContainer galleryContainer;

    public PatternParametersContainer(String title, GalleryContainer galleryContainer) {
        super(title);
        this.galleryContainer = galleryContainer;
        galleryContainer.setParameterPanel(this);
        setupPatternParameters();
    }

    /**
     * Remove all loaded components in containers then load new
     * parameters from the currently chosen pattern.
     */
    void setupPatternParameters() {
        this.removeAll();
        for (PatternParameter paramConfig : galleryContainer.getChosenTemplate().getPatternConfig().values()) {
            this.add(paramConfig.getRenderer());
        }
    }

    /**
     * Remove all loaded components in containers then load new
     * parameters from the given <tt>config</tt> (input will be filtered
     * by attribute's name, type)
     *
     * @param config new configuration
     */
    public void setupPatternParameters(PatternConfig config) {
        this.removeAll();
        for (PatternParameter param : galleryContainer.getChosenTemplate().getPatternConfig().values()) {
            PatternParameter importParam = config.get(param.getCodename());
            // Update current value
            if (importParam != null) {
                param.setCurrentValue(importParam.getCurrentValue());
            }
            // Then show
            this.add(param.getRenderer());
        }
    }
}
