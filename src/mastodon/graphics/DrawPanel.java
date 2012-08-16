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

import java.awt.Color;
import java.awt.Graphics;
import java.util.BitSet;
import java.util.Map;

import javax.swing.JPanel;

/**
 * Taxa pruning frequency heatmap panel.
 * @author justs
 *
 */
public class DrawPanel extends JPanel{
	Map<BitSet, Integer> data;
	
	public DrawPanel(Map<BitSet, Integer> data) {
		this.data = data;
		setBackground(Color.gray);
	}
	
	public void paintComponent(Graphics g) {
		g.setColor(Color.gray);
		super.paintComponents(g);
		
		double max = 0;
		
		for (Map.Entry<BitSet, Integer> entry : data.entrySet()) {
			if(entry.getValue() > max) {
				max = entry.getValue();
			}
		}
		
		int scaleFactor = 5;
		
		for (Map.Entry<BitSet, Integer> entry : data.entrySet()) {
			int x = entry.getKey().nextSetBit(0);
			int y = 0;
			int next = entry.getKey().nextSetBit(x+1);
			if (next < 0) {
				y = x;
			} else {
				y = next;
			}
			int red = (int) (255 * entry.getValue() / max);
			int green = 150 - red;
			if (green < 0) {
				green = 0;
			}
			Color color = new Color(red, red, red);
			g.setColor(color);
			g.fillRect(x*scaleFactor, y*scaleFactor, scaleFactor, scaleFactor);
			
			
		}
		
		
	}
}
