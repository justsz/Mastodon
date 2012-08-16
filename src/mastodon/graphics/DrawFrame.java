/* Copyright (C) 2012 Justs Zarins
 *
 *This file is part of MASTodon.
 *
 *MASTodon is free software: you can redistribute it and/or modify
 *it under the terms of the GNU Lesser General Public License as
 *published by the Free Software Foundation, either version 3
 *of the License, or (at your option) any later version.
 *
 *MASTodon is distributed in the hope that it will be useful,
 *but WITHOUT ANY WARRANTY; without even the implied warranty of
 *MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *GNU Lesser General Public License for more details.
 *
 *You should have received a copy of the GNU Lesser General Public License
 *along with this program.  If not, see http://www.gnu.org/licenses/.
 */
package mastodon.graphics;

import java.util.BitSet;
import java.util.Map;

import javax.swing.JFrame;

/**
 * Taxa pruning frequency heatmap frame.
 * @author justs
 *
 */
public class DrawFrame extends JFrame {
	public DrawFrame(Map<BitSet, Integer> data) {
		DrawPanel panel = new DrawPanel(data);
		getContentPane().add(panel);
		
		setSize(1000, 1000);
	}
}
