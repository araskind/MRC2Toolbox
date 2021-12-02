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

package edu.umich.med.mrc2.datoolbox.misctest;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import edu.umich.med.mrc2.datoolbox.data.compare.SortDirection;
import edu.umich.med.mrc2.datoolbox.gui.utils.ColorUtils;

public class ColorBands {

    public static void main(String[] args) {
        new ColorBands();
    }

    public ColorBands() {
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
                }

                JFrame frame = new JFrame("Testing");
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.setLayout(new BorderLayout());
                frame.add(new TestPane());
                frame.pack();
                frame.setLocationRelativeTo(null);
                frame.setVisible(true);
            }
        });
    }

    public class TestPane extends JPanel {

        private JPanel bandsPane;
        private JSlider slider;
        private Timer changeTimer;

        public TestPane() {
            bandsPane = new JPanel(new GridBagLayout());
            slider = new JSlider(1, 100);
            setLayout(new BorderLayout());
            add(new JScrollPane(bandsPane));
            add(slider, BorderLayout.SOUTH);
            slider.addChangeListener(new ChangeListener() {
                @Override
                public void stateChanged(ChangeEvent e) {
                    changeTimer.restart();
                }
            });

            changeTimer = new Timer(250, new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    int bands = slider.getValue();
                    List<Color> bandsList = 
                    		ColorUtils.getColorBands(Color.RED, bands, SortDirection.ASC);
                    bandsPane.removeAll();
                    GridBagConstraints gbc = new GridBagConstraints();
                    gbc.gridwidth = GridBagConstraints.REMAINDER;
                    gbc.insets = new Insets(1, 1, 1, 1);
                    for (Color color : bandsList) {
                        bandsPane.add(new ColorBand(color), gbc);
                    }
                    gbc.weighty = 1;
                    bandsPane.add(new JPanel(), gbc);
                    revalidate();
                    repaint();
                }
            });
            changeTimer.setRepeats(false);
            slider.setValue(1);
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(200, 200);
        }
    }

    public class ColorBand extends JPanel {

        public ColorBand(Color color) {
            setBackground(color);
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(100, 20);
        }

    }

}
