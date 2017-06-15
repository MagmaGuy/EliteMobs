/*
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.magmaguy.elitemobs.mobcustomizer;

/**
 * Created by MagmaGuy on 18/04/2017.
 */
public class ScalingFormula{

    public static double PowerFormula(double baseAmount, int EliteMobLevel) {

        return 4 * baseAmount * Math.pow((Math.log10(EliteMobLevel)), 1.5) + baseAmount;

    }

    public static double reversePowerFormula (double currentValue, double baseAmount) {

        return Math.pow(10, (Math.pow((currentValue - baseAmount) / (4 * baseAmount), 1 / 1.5)));

    }

}
