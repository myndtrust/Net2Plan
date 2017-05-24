/*******************************************************************************
 * Copyright (c) 2015 Pablo Pavon MariÃ±o.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 * <p>
 * Contributors:
 * Pablo Pavon MariÃ±o - initial API and implementation
 ******************************************************************************/


package com.net2plan.gui.plugins.networkDesign.topologyPane;

import com.net2plan.gui.plugins.networkDesign.visualizationControl.VisualizationConstants;
import com.net2plan.gui.plugins.networkDesign.visualizationControl.VisualizationState;
import com.net2plan.gui.utils.JNumberField;
import com.net2plan.internal.ErrorHandling;
import com.net2plan.utils.SwingUtils;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Point2D;
import java.util.Arrays;
import java.util.List;

/**
 * @author Javier Lopez
 */
public final class LinkStyleSelector extends JDialog implements ActionListener
{
    private final VisualizationState _visualizationState;
    private final Color _errorBackgroundColor = new Color(255, 50, 50);

    public LinkStyleSelector(VisualizationState visualizationState)
    {
        super();

        _visualizationState = visualizationState;

        this.setTitle("Link Style");
        this.setLayout(new BorderLayout());

        JTabbedPane tabbedPane = new JTabbedPane();

        //Link-utilization coloring Tab
        tabbedPane.addTab("Link-utilization coloring", getLinkUtilizationColoringPanel());

        //Link run-out time coloring Tab
//        tabbedPane.addTab("Link run-out time coloring", getLinkRunoutTimeColoringPanel());

        //Link thickness Tab
//        tabbedPane.addTab("Link relative thickness", getLinkThicknessPanel());

        this.add(tabbedPane, BorderLayout.CENTER);
        this.pack();

        SwingUtils.configureCloseDialogOnEscape(this);
        this.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
        this.pack();
        this.setResizable(true);
        this.setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e)
    {
    }

    public JPanel getLinkUtilizationColoringPanel()
    {
        List<Double> linkUtilizationColor = _visualizationState.getLinkUtilizationColor();

        if (linkUtilizationColor.size() != VisualizationConstants.DEFAULT_LINKCOLORINGUTILIZATIONTHRESHOLDS.size())
            throw new RuntimeException();

        //Create JTextField array
        JNumberField[] fieldArray = new JNumberField[linkUtilizationColor.size()];
        Arrays.fill(fieldArray, new JNumberField());

        for (int i = 0; i < fieldArray.length; i++)
        {
            final double maxIncrement = 1e-2;

            final JNumberField numberField = fieldArray[i];

            final JSpinner.NumberEditor editor = (JSpinner.NumberEditor) numberField.getEditor();
            editor.getFormat().setMaximumFractionDigits(2);

            final SpinnerNumberModel model = (SpinnerNumberModel) numberField.getModel();
            model.setStepSize(maxIncrement);
            model.setValue(linkUtilizationColor.get(i));

            numberField.addChangeListener(new ChangeListener()
            {
                @Override
                public void stateChanged(ChangeEvent changeEvent)
                {
                    for (int j = 0; j < fieldArray.length; j++)
                    {
                        final double lowerValue = j == 0 ? 0 : fieldArray[j - 1].getValue() + maxIncrement;
                        final double upperValue = j == fieldArray.length - 1 ? 100 : fieldArray[j + 1].getValue() - maxIncrement;
                        final JNumberField numberField = fieldArray[j];

                        final SpinnerNumberModel model = (SpinnerNumberModel) numberField.getModel();

                        model.setMinimum(lowerValue);
                        model.setMaximum(upperValue);
                    }
                }
            });

            fieldArray[i] = numberField;
        }

        Rectangle[] rectangleArray = new Rectangle[VisualizationConstants.DEFAULT_LINKCOLORSPERUTILIZATIONANDRUNOUT.size()];

        for (int i = 0; i < rectangleArray.length; i++)
            rectangleArray[i] = new Rectangle(VisualizationConstants.DEFAULT_LINKCOLORSPERUTILIZATIONANDRUNOUT.get(i));

        JToggleButton btn_apply = new JToggleButton("Is applied", _visualizationState.getIsActiveLinkUtilizationColorThresholdList());
        btn_apply.setToolTipText("The link coloring per utilization is active or not");
        btn_apply.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                _visualizationState.setIsActiveLinkUtilizationColorThresholdList(btn_apply.isSelected());
            }
        });

        JButton btn_save = new JButton("Save");
        btn_save.setToolTipText("Save the current selection");
        btn_save.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                for (int i = 0; i < linkUtilizationColor.size(); i++)
                    linkUtilizationColor.set(i, fieldArray[i].getValue());

                _visualizationState.setLinkUtilizationColor(linkUtilizationColor);

                dispose();
            }
        });

        JButton btn_cancel = new JButton("Cancel");
        btn_cancel.setToolTipText("Close the dialog without saving");
        btn_cancel.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                dispose();
            }
        });

        JButton btn_reset = new JButton("Reset");
        btn_reset.setToolTipText("Reset all options");
        btn_reset.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                linkUtilizationColor.clear();
                linkUtilizationColor.addAll(VisualizationConstants.DEFAULT_LINKCOLORINGUTILIZATIONTHRESHOLDS);
                for (int i = 0; i < linkUtilizationColor.size(); i++)
                {
                    fieldArray[i].setValue(linkUtilizationColor.get(i));
                    fieldArray[i].setBackground(Color.WHITE);
                }
            }
        });

        JLabel label_100 = new JLabel("100");
        label_100.setHorizontalAlignment(SwingConstants.RIGHT);

        JLabel label_0 = new JLabel("0");
        label_0.setHorizontalAlignment(SwingConstants.RIGHT);

        JPanel pane = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(0, 5, 5, 5);
        gbc.gridwidth = 2;
        pane.add(new JLabel("Color"), gbc);
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        pane.add(new JLabel("Utilization (%)"), gbc);

        gbc.gridwidth = 1;
        pane.add(rectangleArray[rectangleArray.length - 1], gbc);
        pane.add(new Label(">="), gbc);
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        pane.add(label_100, gbc);

        for (int i = fieldArray.length - 1; i >= 0; i--)
        {
            gbc.gridwidth = 1;
            pane.add(rectangleArray[i + 1], gbc);
            pane.add(new Label(">="), gbc);
            gbc.gridwidth = GridBagConstraints.REMAINDER;
            pane.add(fieldArray[i], gbc);
        }

        gbc.gridwidth = 1;
        pane.add(rectangleArray[0], gbc);
        pane.add(new Label(">="), gbc);
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        pane.add(label_0, gbc);

        gbc.gridwidth = 1;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        pane.add(btn_apply, gbc);

        //Main pane
        JPanel mainPane = new JPanel(new BorderLayout());

        //Buttons bar
        JPanel buttonBar = new JPanel();
        buttonBar.add(btn_save);
        buttonBar.add(btn_reset);
        buttonBar.add(btn_cancel);

        mainPane.add(pane, BorderLayout.CENTER);
        mainPane.add(buttonBar, BorderLayout.SOUTH);

        return mainPane;
    }

    public JPanel getLinkRunoutTimeColoringPanel()
    {
        List<Double> linkRunoutTimeColor = _visualizationState.getLinkRunoutTimeColor();

        if (linkRunoutTimeColor.size() != VisualizationConstants.DEFAULT_LINKCOLORINGRUNOUTTHRESHOLDS.size())
            throw new RuntimeException();

        //Create JTextField array
        JTextField[] fieldArray = new JTextField[linkRunoutTimeColor.size()];

        for (int i = 0; i < linkRunoutTimeColor.size(); i++)
        {
            fieldArray[i] = new JTextField("" + linkRunoutTimeColor.get(i));
            fieldArray[i].setHorizontalAlignment(SwingConstants.RIGHT);
        }

        Rectangle[] rectangleArray = new Rectangle[VisualizationConstants.DEFAULT_LINKCOLORSPERUTILIZATIONANDRUNOUT.size()];

        for (int i = 0; i < rectangleArray.length; i++)
            rectangleArray[i] = new Rectangle(VisualizationConstants.DEFAULT_LINKCOLORSPERUTILIZATIONANDRUNOUT.get(i));

        JToggleButton btn_apply = new JToggleButton("Is applied", _visualizationState.getIsActiveLinkRunoutTimeColorThresholdList());
        btn_apply.setToolTipText("The link coloring per run-out capacity time is active or not");
        btn_apply.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                _visualizationState.setIsActiveLinkRunoutTimeColorThresholdList(btn_apply.isSelected());
            }
        });

        JButton btn_save = new JButton("Save");
        btn_save.setToolTipText("Save the current selection");
        btn_save.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                boolean isValid = true;

                for (int i = 0; i < linkRunoutTimeColor.size(); i++)
                {
                    double value = -1;

                    if (fieldArray[i].getText().matches("[0-9]+\\.*[0-9]*"))
                    {
                        try
                        {
                            value = Double.parseDouble(fieldArray[i].getText());
                        } catch (NumberFormatException nfe)
                        {
                        }
                    }

                    double previousValue = -1;

                    if (i == linkRunoutTimeColor.size() - 1)
                        previousValue = Double.MAX_VALUE;
                    else if (fieldArray[i + 1].getText().matches("[0-9]+\\.*[0-9]*"))
                    {
                        try
                        {
                            previousValue = Double.parseDouble(fieldArray[i + 1].getText());
                        } catch (NumberFormatException nfe)
                        {
                        }
                    }

                    if (value != -1)
                    {
                        if (previousValue != -1 && value >= previousValue)
                        {
                            fieldArray[i].setBackground(_errorBackgroundColor);
                            isValid = false;
                        } else
                            fieldArray[i].setBackground(Color.WHITE);
                    } else
                    {
                        fieldArray[i].setBackground(_errorBackgroundColor);
                        isValid = false;
                    }
                }

                if (isValid)
                {
                    for (int i = 0; i < linkRunoutTimeColor.size(); i++)
                        linkRunoutTimeColor.set(i, Double.parseDouble(fieldArray[i].getText()));

                    _visualizationState.setLinkRunoutTimeColor(linkRunoutTimeColor);
                    dispose();
                } else
                {
                    ErrorHandling.showErrorDialog("Some fields are incorrect!", "Error");
                }
            }
        });

        JButton btn_cancel = new JButton("Cancel");
        btn_cancel.setToolTipText("Close the dialog without saving");
        btn_cancel.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                dispose();
            }
        });

        JButton btn_reset = new JButton("Reset");
        btn_reset.setToolTipText("Reset all options");
        btn_reset.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                linkRunoutTimeColor.clear();
                linkRunoutTimeColor.addAll(VisualizationConstants.DEFAULT_LINKCOLORINGRUNOUTTHRESHOLDS);
                for (int i = 0; i < linkRunoutTimeColor.size(); i++)
                {
                    fieldArray[i].setText("" + linkRunoutTimeColor.get(i));
                    fieldArray[i].setBackground(Color.WHITE);
                }
            }
        });

        JLabel label_0 = new JLabel("0");
        label_0.setHorizontalAlignment(SwingConstants.RIGHT);

        JPanel pane = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(0, 5, 5, 5);
        gbc.gridwidth = 1;
        pane.add(new JLabel("Color"), gbc);
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        pane.add(new JLabel("Run-out time (months)"), gbc);

        for (int i = fieldArray.length - 1; i >= 0; i--)
        {
            gbc.gridwidth = 1;
            pane.add(rectangleArray[i + 1], gbc);
            pane.add(new Label(">="), gbc);
            gbc.gridwidth = GridBagConstraints.REMAINDER;
            pane.add(fieldArray[i], gbc);
        }

        gbc.gridwidth = 1;
        pane.add(rectangleArray[0], gbc);
        pane.add(new Label(">="), gbc);
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        pane.add(label_0, gbc);

        //Main pane
        JPanel mainPane = new JPanel(new BorderLayout());

        //Buttons bar
        JPanel buttonBar = new JPanel();

        buttonBar.add(btn_save);
        buttonBar.add(btn_apply);
        buttonBar.add(btn_reset);
        buttonBar.add(btn_cancel);

        mainPane.add(pane, BorderLayout.CENTER);
        mainPane.add(buttonBar, BorderLayout.SOUTH);

        return mainPane;
    }

    public JPanel getLinkThicknessPanel()
    {
        List<Double> linkCapacityThickness = _visualizationState.getLinkCapacityThickness();

        //Create JTextField array
        JTextField[] fieldArray = new JTextField[linkCapacityThickness.size()];

        for (int i = 0; i < linkCapacityThickness.size(); i++)
        {
            fieldArray[i] = new JTextField("" + linkCapacityThickness.get(i));
            fieldArray[i].setHorizontalAlignment(SwingConstants.RIGHT);
        }

        //Create Line array
        Line[] lineArray = new Line[VisualizationConstants.DEFAULT_LINKRELATIVETHICKNESSVALUES.size()];

        for (int i = 0; i < lineArray.length; i++)
            lineArray[i] = new Line(i + 1);


        JToggleButton btn_apply = new JToggleButton("Is applied", _visualizationState.getIsActiveLinkCapacityThicknessThresholdList());
        btn_apply.setToolTipText("The link thickness dependent on its capacity is active or not");
        btn_apply.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                _visualizationState.setIsActiveLinkCapacityThicknessThresholdList(btn_apply.isSelected());
            }
        });


        JButton btn_save = new JButton("Save");
        btn_save.setToolTipText("Save the current selection");
        btn_save.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                boolean isValid = true;

                for (int i = 0; i < linkCapacityThickness.size(); i++)
                {
                    double value = -1;

                    if (fieldArray[i].getText().matches("[0-9]+\\.*[0-9]*"))
                    {
                        try
                        {
                            value = Double.parseDouble(fieldArray[i].getText());
                        } catch (NumberFormatException nfe)
                        {
                        }
                    }

                    double previousValue = -1;

                    if (i == linkCapacityThickness.size() - 1)
                        previousValue = Double.MAX_VALUE;
                    else if (fieldArray[i + 1].getText().matches("[0-9]+\\.*[0-9]*"))
                    {
                        try
                        {
                            previousValue = Double.parseDouble(fieldArray[i + 1].getText());
                        } catch (NumberFormatException nfe)
                        {
                        }
                    }

                    if (value != -1)
                    {
                        if (previousValue != -1 && value >= previousValue)
                        {
                            fieldArray[i].setBackground(_errorBackgroundColor);
                            isValid = false;
                        } else
                            fieldArray[i].setBackground(Color.WHITE);
                    } else
                    {
                        fieldArray[i].setBackground(_errorBackgroundColor);
                        isValid = false;
                    }
                }

                if (isValid)
                {
                    for (int i = 0; i < linkCapacityThickness.size(); i++)
                        linkCapacityThickness.set(i, Double.parseDouble(fieldArray[i].getText()));

                    _visualizationState.setLinkCapacityThickness(linkCapacityThickness);

                    dispose();
                } else
                {
                    ErrorHandling.showErrorDialog("Some fields are incorrect!", "Error");
                }
            }
        });

        JButton btn_cancel = new JButton("Cancel");
        btn_cancel.setToolTipText("Close the dialog without saving");
        btn_cancel.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                dispose();
            }
        });

        JButton btn_reset = new JButton("Reset");
        btn_reset.setToolTipText("Reset all options");
        btn_reset.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                linkCapacityThickness.clear();
                linkCapacityThickness.addAll(VisualizationConstants.DEFAULT_LINKTHICKNESSTHRESHPOLDS);
                for (int i = 0; i < linkCapacityThickness.size(); i++)
                {
                    fieldArray[i].setText("" + linkCapacityThickness.get(i));
                    fieldArray[i].setBackground(Color.WHITE);
                }
            }
        });

        JLabel label_0 = new JLabel("0");
        label_0.setHorizontalAlignment(SwingConstants.RIGHT);

        JPanel pane = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(0, 5, 5, 5);
        gbc.gridwidth = 1;
        pane.add(new JLabel("Thickness"), gbc);
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        pane.add(new JLabel("Capacity (Gbps)"), gbc);

        for (int i = fieldArray.length - 1; i >= 0; i--)
        {
            gbc.gridwidth = 1;
            pane.add(lineArray[i + 1], gbc);
            pane.add(new Label(">="), gbc);
            gbc.gridwidth = GridBagConstraints.REMAINDER;
            pane.add(fieldArray[i], gbc);
        }

        gbc.gridwidth = 1;
        pane.add(lineArray[0], gbc);
        pane.add(new Label(">="), gbc);
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        pane.add(label_0, gbc);

        //Main pane
        JPanel mainPane = new JPanel(new BorderLayout());

        //Buttons bar
        JPanel buttonBar = new JPanel();

        buttonBar.add(btn_save);
        buttonBar.add(btn_apply);
        buttonBar.add(btn_reset);
        buttonBar.add(btn_cancel);

        mainPane.add(pane, BorderLayout.CENTER);
        mainPane.add(buttonBar, BorderLayout.SOUTH);

        return mainPane;
    }
}

class Rectangle extends JPanel
{
    private Color color;

    public Rectangle(Color color)
    {
        this.color = color;
    }

    @Override
    public Dimension getPreferredSize()
    {
        return new Dimension(30, 20);
    }

    @Override
    protected void paintComponent(Graphics g)
    {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();

        g2d.setColor(this.color);
        g2d.fillRect(0, 0, 30, 20);
        g2d.dispose();
    }
}

class Line extends JPanel
{
    private Point p1 = new Point(0, 8);
    private Point p2 = new Point(30, 8);
    private int height;

    public Line(int height)
    {
        this.height = height;
    }

    @Override
    public Dimension getPreferredSize()
    {
        return new Dimension(20, 20);
    }

    @Override
    protected void paintComponent(Graphics g)
    {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setColor(Color.BLACK);
        for (double t = 0; t < 1; t += 0.01)
        {
            Point2D p = between(p1, p2, t);
            g2d.fillRect((int) p.getX(), (int) p.getY(), 5, height);
        }
        g2d.dispose();
    }

    public Point2D between(Point p1, Point p2, double time)
    {
        double deltaX = p2.getX() - p1.getX();
        double deltaY = p2.getY() - p1.getY();

        double x = p1.getX() + time * deltaX;
        double y = p1.getY() + time * deltaY;

        return new Point2D.Double(x, y);

    }
}


