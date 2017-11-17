/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import javax.swing.*;
//import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import populate.Populate;

public class hw3 extends JFrame {

    public static final String DATE_FORMAT = "yyyy-MM-dd";
    private static HashSet<String> selectedMainCategoriesSet = new HashSet();
    private static HashSet<String> allSubCategoriesSet = new HashSet();
    private static HashSet<String> selectedSubCategoriesSet = new HashSet();
    private static HashSet<String> allAttributesSet = new HashSet();
    private static HashSet<String> selectedAttributesSet = new HashSet();

    private static StringBuilder mainCategoriesString = new StringBuilder();
    private static StringBuilder subCategoriesString = new StringBuilder();
    private static StringBuilder attributesString = new StringBuilder();

    /**
     * Creates new form main
     */
    public hw3() {
        initComponents();
        try {
            init();
        } catch (SQLException ex) {
            Logger.getLogger(hw3.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(hw3.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void ERROR(String msg) {
//        queryTextArea.append(msg);
    }

    private void init() throws SQLException, ClassNotFoundException {
        System.out.println("+++init+++");
        try (Connection connection = Populate.getConnect();) {
            StringBuilder sql = new StringBuilder();
            PreparedStatement preparedStatement;
            ResultSet rs;

            //init radioButton
//            ButtonGroup group = new ButtonGroup();
//            group.add(businessRadioButton);
//            group.add(userRadioButton);
            //init mainCategory
            sql.append("SELECT DISTINCT mainCategory FROM MainCategory ORDER BY mainCategory");
            preparedStatement = connection.prepareStatement(sql.toString());
            rs = preparedStatement.executeQuery();
            while (rs.next()) {
                String mainCategoryName = rs.getString(rs.findColumn("mainCategory"));
                JCheckBox checkBox = new JCheckBox(mainCategoryName);
                checkBox.addMouseListener(new MouseListener() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        try {
                            JCheckBox mc = (JCheckBox) e.getSource();
                            String mainCategory = mc.getText();
                            if (mc.isSelected()) {
                                selectedMainCategoriesSet.add(mainCategory);
                            } else {
                                selectedMainCategoriesSet.remove(mainCategory);
                            }
                            // get mainCategories from hashSet to arrayList
                            mainCategoriesString = new StringBuilder();
                            Iterator<String> it = selectedMainCategoriesSet.iterator();
                            while (it.hasNext()) {
                                mainCategoriesString.append("'").append(it.next()).append("',");
                            }
                            if (mainCategoriesString.length() > 0) {
                                mainCategoriesString.deleteCharAt(mainCategoriesString.length() - 1);
                            }
                            System.out.println("DEBUG=========== select mainCategories: " + mainCategoriesString.toString());
                            updateSubCategories();
                        } catch (SQLException ex) {
                            Logger.getLogger(hw3.class.getName()).log(Level.SEVERE, null, ex);
                        } catch (ClassNotFoundException ex) {
                            Logger.getLogger(hw3.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }

                    @Override
                    public void mousePressed(MouseEvent e) {
                    }

                    @Override
                    public void mouseReleased(MouseEvent e) {
                    }

                    @Override
                    public void mouseEntered(MouseEvent e) {
                    }

                    @Override
                    public void mouseExited(MouseEvent e) {
                    }
                });
                mCategoryListPanel.add(checkBox);
            }
            rs.close();
            preparedStatement.close();
        }
    }

    private String getAndQuery(String name, HashSet<String> values) {
        if (values.isEmpty()) {
            return "1 = 0";
        }
        StringBuilder sql = new StringBuilder();
        boolean first = true;
        for (String value : values) {
            if (first) {
                first = false;
            } else {
                sql.append(" AND ");
            }
            sql.append(name);
            sql.append(" = ");
            sql.append("\"").append(value).append("\"");

        }
        return sql.toString();
    }

    private String getOrQuery(String name, HashSet<String> values) {
        if (values.isEmpty()) {
            return "1 = 0";
        }
        StringBuilder sql = new StringBuilder();
        sql.append(name);
        sql.append(" in (");
        boolean first = true;
        for (String value : values) {
            if (first) {
                first = false;
            } else {
                sql.append(" , ");
            }
            sql.append("\"").append(value).append("\"");
        }
        sql.append(")");
        return sql.toString();
    }

    private String getQuery(String name, HashSet<String> values) {
        return getOrQuery(name, values);
    }

    private void updateSubCategories() throws SQLException, ClassNotFoundException {
        try (Connection connection = Populate.getConnect()) {
            sCategoryListPanel.removeAll();
            System.out.println("Updating subCategories...");
            PreparedStatement preparedStatement;
            ResultSet rs;
            StringBuilder sql = new StringBuilder();
            sql.append("SELECT DISTINCT sc.subCategory").append("\n")
                    .append("FROM SubCategory sc, MainCategory mc").append("\n")
                    .append("WHERE sc.business_id = mc.business_id AND ")
                    .append(getQuery("mc.mainCategory", selectedMainCategoriesSet)).append("\n")
                    .append("ORDER BY sc.subCategory");
            System.out.println("DEBUG=========== select subCategories: " + sql.toString() + "\n");
            preparedStatement = connection.prepareStatement(sql.toString());
            rs = preparedStatement.executeQuery();
            while (rs.next()) {
                String subCategory = rs.getString(rs.findColumn("subCategory"));
                allSubCategoriesSet.add(subCategory);
            }
            rs.close();
            preparedStatement.close();
            for (String scName : allSubCategoriesSet) {
                JCheckBox sc = new JCheckBox(scName);
                sc.addMouseListener(new MouseListener() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        JCheckBox sc = (JCheckBox) e.getSource();
                        String subCategory = sc.getText();
                        if (sc.isSelected()) {
                            selectedSubCategoriesSet.add(subCategory);
                        } else {
                            selectedSubCategoriesSet.remove(subCategory);
                        }
                        subCategoriesString.setLength(0);
                        Iterator<String> it = selectedSubCategoriesSet.iterator();
                        while (it.hasNext()) {
                            subCategoriesString.append("'").append(it.next()).append("',");
                        }
                        if (subCategoriesString.length() > 0) {
                            subCategoriesString.deleteCharAt(subCategoriesString.length() - 1);
                        }
                        System.out.println("DEBUG=========== select subCategories: " + subCategoriesString.toString() + "\n");
                        try {
                            updateAttributes();
                        } catch (SQLException ex) {
                            Logger.getLogger(hw3.class.getName()).log(Level.SEVERE, null, ex);
                        } catch (ClassNotFoundException ex) {
                            Logger.getLogger(hw3.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }

                    @Override
                    public void mousePressed(MouseEvent e) {
                    }

                    @Override
                    public void mouseReleased(MouseEvent e) {
                    }

                    @Override
                    public void mouseEntered(MouseEvent e) {
                    }

                    @Override
                    public void mouseExited(MouseEvent e) {
                    }
                });
                sCategoryListPanel.add(sc);
            }
            sCategoryListPanel.updateUI();
        }
    }

    private void updateAttributes() throws SQLException, ClassNotFoundException {
        try (Connection connection = Populate.getConnect()) {
            attributeListPanel.removeAll();
            System.out.println("Get attributes...");

            StringBuilder sql = new StringBuilder();
            PreparedStatement preparedStatement;
            ResultSet rs;
            sql.append("SELECT a.attribute\n")
                    .append("FROM Attribute a, MainCategory mc, SubCategory sc\n")
                    .append("WHERE a.business_id = mc.business_id AND a.business_id = sc.business_id")
                    .append(" AND ").append(getQuery("mc.mainCategory", selectedMainCategoriesSet)).append(" AND ")
                    .append(getQuery("sc.subCategory", selectedSubCategoriesSet)).append("\n")
                    .append("ORDER BY a.attribute\n");
            System.out.println("DEBUG============== select attributes:\n" + sql.toString());
            preparedStatement = connection.prepareStatement(sql.toString());
            rs = preparedStatement.executeQuery();
            while (rs.next()) {
                String attribute = rs.getString(rs.findColumn("attribute"));
                allAttributesSet.add(attribute);
            }
            rs.close();
            preparedStatement.close();

            for (String aName : allAttributesSet) {
                JCheckBox a = new JCheckBox(aName);
                a.addMouseListener(new MouseListener() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        JCheckBox a = (JCheckBox) e.getSource();
                        String attribute = a.getText();
                        if (a.isSelected()) {
                            selectedAttributesSet.add(attribute);
                        } else {
                            selectedAttributesSet.remove(attribute);
                        }
                        attributesString.setLength(0);
                        Iterator<String> it = selectedAttributesSet.iterator();
                        while (it.hasNext()) {
                            attributesString.append("'").append(it.next()).append("',");
                        }
                        if (attributesString.length() > 0) {
                            attributesString.deleteCharAt(attributesString.length() - 1);
                        }
                        System.out.println("DEBUG=========== attributes: " + attributesString.toString());
                        updateItem();
                    }

                    @Override
                    public void mousePressed(MouseEvent e) {
                    }

                    @Override
                    public void mouseReleased(MouseEvent e) {
                    }

                    @Override
                    public void mouseEntered(MouseEvent e) {
                    }

                    @Override
                    public void mouseExited(MouseEvent e) {
                    }

                });
                attributeListPanel.add(a);
            }
            attributeListPanel.updateUI();
        }
    }

    private void updateItem() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        View = new javax.swing.JPanel();
        businessPanel = new javax.swing.JPanel();
        categoriesPanel = new javax.swing.JPanel();
        mainCategoryPanel = new javax.swing.JPanel();
        categoryLabel = new javax.swing.JLabel();
        mainCategoryScrollPane = new javax.swing.JScrollPane();
        mCategoryListPanel = new javax.swing.JPanel();
        subCategoryPanel = new javax.swing.JPanel();
        subCategoryLabel = new javax.swing.JLabel();
        subCategoryScrollPane = new javax.swing.JScrollPane();
        sCategoryListPanel = new javax.swing.JPanel();
        attributePanel = new javax.swing.JPanel();
        attributeLabel = new javax.swing.JLabel();
        attributeScrollPane = new javax.swing.JScrollPane();
        attributeListPanel = new javax.swing.JPanel();
        queryPanel = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        resultTable = new javax.swing.JTable();
        Selection = new javax.swing.JPanel();
        queryButton = new javax.swing.JButton();
        queryButton1 = new javax.swing.JButton();
        day = new javax.swing.JComboBox<>();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        from = new javax.swing.JComboBox<>();
        jLabel3 = new javax.swing.JLabel();
        to = new javax.swing.JComboBox<>();
        jLabel4 = new javax.swing.JLabel();
        location = new javax.swing.JComboBox<>();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setBackground(new java.awt.Color(0, 0, 153));
        setPreferredSize(null);

        View.setBackground(new java.awt.Color(255, 204, 255));
        View.setToolTipText("hw3");
        View.setMinimumSize(new java.awt.Dimension(390, 52));
        View.setPreferredSize(new java.awt.Dimension(1000, 500));
        View.setLayout(new java.awt.GridLayout(1, 2));

        businessPanel.setBackground(new java.awt.Color(0, 0, 204));
        businessPanel.setBorder(javax.swing.BorderFactory.createCompoundBorder());
        businessPanel.setToolTipText("Business");
        businessPanel.setLayout(new java.awt.BorderLayout());

        categoriesPanel.setLayout(new java.awt.GridLayout(1, 3));

        mainCategoryPanel.setLayout(new java.awt.BorderLayout());

        categoryLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        categoryLabel.setText("Category");
        mainCategoryPanel.add(categoryLabel, java.awt.BorderLayout.PAGE_START);

        mCategoryListPanel.setBackground(new java.awt.Color(153, 153, 255));
        mCategoryListPanel.setLayout(new java.awt.GridLayout(0, 1));

        //for (int i = 0; i < 20; i++) {
            //    mCategoryListPanel.add(new JCheckBox("aaa"));
            //    if (i == 18) {
                //        ((JCheckBox) mCategoryListPanel.getComponent(i)).setSelected(true);
                //    }
            //}

        mainCategoryScrollPane.setViewportView(mCategoryListPanel);

        mainCategoryPanel.add(mainCategoryScrollPane, java.awt.BorderLayout.CENTER);

        categoriesPanel.add(mainCategoryPanel);

        subCategoryPanel.setLayout(new java.awt.BorderLayout());

        subCategoryLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        subCategoryLabel.setText("Sub-category");
        subCategoryPanel.add(subCategoryLabel, java.awt.BorderLayout.PAGE_START);

        sCategoryListPanel.setBackground(new java.awt.Color(102, 153, 255));
        sCategoryListPanel.setLayout(new java.awt.GridLayout(0, 1));

        //for (int i = 0; i < 20; i++) {
            //    sCategoryListPanel.add(new JCheckBox("bbbb"));
            //    if (i == 1) {
                //        ((JCheckBox) sCategoryListPanel.getComponent(i)).setSelected(true);
                //    }
            //}

        subCategoryScrollPane.setViewportView(sCategoryListPanel);

        subCategoryPanel.add(subCategoryScrollPane, java.awt.BorderLayout.CENTER);

        categoriesPanel.add(subCategoryPanel);

        attributePanel.setLayout(new java.awt.BorderLayout());

        attributeLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        attributeLabel.setText("Attribute");
        attributePanel.add(attributeLabel, java.awt.BorderLayout.PAGE_START);

        attributeListPanel.setBackground(new java.awt.Color(204, 255, 255));
        attributeListPanel.setLayout(new java.awt.GridLayout(0, 1));

        //for (int i = 0; i < 20; i++) {
            //    attributeListPanel.add(new JCheckBox("ccc"));
            //    if (i == 2) {
                //        ((JCheckBox) attributeListPanel.getComponent(i)).setSelected(true);
                //    }
            //}

        attributeScrollPane.setViewportView(attributeListPanel);

        attributePanel.add(attributeScrollPane, java.awt.BorderLayout.CENTER);

        categoriesPanel.add(attributePanel);

        businessPanel.add(categoriesPanel, java.awt.BorderLayout.CENTER);

        View.add(businessPanel);

        queryPanel.setToolTipText("");
        queryPanel.setPreferredSize(new java.awt.Dimension(500, 200));

        resultTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "id", "address", "city", "state", "stars", "reviews", "check-ins"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.Float.class, java.lang.Integer.class, java.lang.Object.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        resultTable.setRequestFocusEnabled(false);
        jScrollPane1.setViewportView(resultTable);

        javax.swing.GroupLayout queryPanelLayout = new javax.swing.GroupLayout(queryPanel);
        queryPanel.setLayout(queryPanelLayout);
        queryPanelLayout.setHorizontalGroup(
            queryPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(queryPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 511, Short.MAX_VALUE)
                .addContainerGap())
        );
        queryPanelLayout.setVerticalGroup(
            queryPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(queryPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 519, Short.MAX_VALUE))
        );

        View.add(queryPanel);

        Selection.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        Selection.setPreferredSize(new java.awt.Dimension(1063, 100));

        queryButton.setBackground(new java.awt.Color(255, 0, 255));
        queryButton.setText("Search");
        queryButton.setToolTipText("");
        queryButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                queryButtonMouseClicked(evt);
            }
        });

        queryButton1.setBackground(new java.awt.Color(255, 0, 255));
        queryButton1.setText("Close");
        queryButton1.setToolTipText("");
        queryButton1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                queryButton1MouseClicked(evt);
            }
        });

        day.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] {"", "Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday" }));
        day.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dayActionPerformed(evt);
            }
        });

        jLabel1.setText("Day of the week:");

        jLabel2.setText("From:");

        from.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] {  }));
        from.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fromActionPerformed(evt);
            }
        });

        jLabel3.setText("To:");

        to.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { }));

        jLabel4.setText("Search For:");

        location.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] {  }));

        javax.swing.GroupLayout SelectionLayout = new javax.swing.GroupLayout(Selection);
        Selection.setLayout(SelectionLayout);
        SelectionLayout.setHorizontalGroup(
            SelectionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(SelectionLayout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addGroup(SelectionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1)
                    .addComponent(day, javax.swing.GroupLayout.PREFERRED_SIZE, 97, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(57, 57, 57)
                .addGroup(SelectionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel2)
                    .addComponent(from, javax.swing.GroupLayout.PREFERRED_SIZE, 86, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(52, 52, 52)
                .addGroup(SelectionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(SelectionLayout.createSequentialGroup()
                        .addComponent(jLabel3)
                        .addGap(148, 148, 148)
                        .addComponent(jLabel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGap(307, 307, 307))
                    .addGroup(SelectionLayout.createSequentialGroup()
                        .addComponent(to, javax.swing.GroupLayout.PREFERRED_SIZE, 89, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(location, javax.swing.GroupLayout.PREFERRED_SIZE, 155, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(208, 208, 208)))
                .addComponent(queryButton, javax.swing.GroupLayout.PREFERRED_SIZE, 88, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(queryButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 88, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(32, 32, 32))
        );
        SelectionLayout.setVerticalGroup(
            SelectionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(SelectionLayout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addGroup(SelectionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, SelectionLayout.createSequentialGroup()
                        .addGroup(SelectionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(queryButton)
                            .addComponent(queryButton1))
                        .addGap(26, 26, 26))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, SelectionLayout.createSequentialGroup()
                        .addGroup(SelectionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel1)
                            .addComponent(jLabel2)
                            .addComponent(jLabel3)
                            .addComponent(jLabel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(SelectionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(day, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(from, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(to, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(location, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addContainerGap())))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(View, javax.swing.GroupLayout.PREFERRED_SIZE, 1063, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addComponent(Selection, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(View, javax.swing.GroupLayout.PREFERRED_SIZE, 530, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(Selection, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void queryButtonMouseClicked(MouseEvent evt) {//GEN-FIRST:event_queryButtonMouseClicked
        // TODO add your handling code here:
        DefaultTableModel defaultTableModel;
        String[][] data;

        try (Connection connection = Populate.getConnect();) {
            StringBuilder query = new StringBuilder();
            PreparedStatement preparedStatement;
            ResultSet rs;
//            queryTextArea.setText("<Show Query Here:> \n\n");
            // if it's business turn
            if (businessRadioButton.isSelected()) {
                if (mainCategoriesString.length() == 0) {
                    ERROR("ERROR: ON ACTION ON SELECT \"Category\"!\n\n");
                    return;
                } else {
                    query = getBusinessQueryString();
                }
            } // if it's user turn
            else if (userRadioButton.isSelected()) {
                query = getUserQueryString();
            } else {
                ERROR("ERROR: ON ACTION SELECT BUSINESS INTERFACE OR USER INTERFACE!\n\n");
                return;
            }

            if (query.length() == 0) {
                ERROR("ERROR: NO ACTION!\n\n");
                return;
            }
            preparedStatement = connection.prepareStatement(query.toString(), ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            rs = preparedStatement.executeQuery();

            rs.last();
            ResultSetMetaData rsmd = rs.getMetaData();
            int rowCount = rs.getRow();
            int columnCount = rsmd.getColumnCount();
            data = new String[rowCount][columnCount];
            String[] columnNames = new String[columnCount];

            // get column names
            for (int i = 1; i <= columnCount; i++) {
                columnNames[i - 1] = rsmd.getColumnName(i);
            }

            rs.beforeFirst();
            for (int i = 0; i < rowCount; i++) {
                if (rs.next()) {
                    for (int j = 1; j <= columnCount; j++) {
                        data[i][j - 1] = rs.getString(j);
                    }
                }
            }
            rs.close();
            preparedStatement.close();
            defaultTableModel = new DefaultTableModel(data, columnNames);
            resultTable.setModel(defaultTableModel);
            queryTextArea.append(query.toString());

            // lisening to resultTable and get the information of review
            resultTable.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                    if (e.getClickCount() == 1) {
                        JTable target = (JTable) e.getSource();
                        int row = target.getSelectedRow();
                        String id = resultTable.getModel().getValueAt(row, 1).toString();
                        try {
                            showReview(id);
                        } catch (SQLException ex) {
                            Logger.getLogger(hw3.class.getName()).log(Level.SEVERE, null, ex);
                        } catch (ClassNotFoundException ex) {
                            Logger.getLogger(hw3.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }
            });

        } catch (SQLException ex) {
            Logger.getLogger(hw3.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(hw3.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_queryButtonMouseClicked

    private void queryButton1MouseClicked(MouseEvent evt) {//GEN-FIRST:event_queryButton1MouseClicked
        // TODO add your handling code here:
    }//GEN-LAST:event_queryButton1MouseClicked

    private void dayActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dayActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_dayActionPerformed

    private void fromActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fromActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_fromActionPerformed

    private StringBuilder getBusinessQueryString() {
        StringBuilder query = new StringBuilder();

        //get query from category
        query.append("SELECT b.name, b.business_id, b.city, b.state, b.stars, mc.MainCategory\n")
                .append("FROM Business b, MainCategory mc\n")
                .append("WHERE b.business_id = mc.business_id AND mc.mainCategory IN (").append(mainCategoriesString).append(")\n");

        //check subcategory and get query from subCategory
        if (subCategoriesString.length() != 0) {
            query.append("\nAND b.business_id IN (\n")
                    .append("  SELECT bc.business_id\n")
                    .append("  FROM SubCategory bc\n")
                    .append("  WHERE bc.subCategory IN (").append(subCategoriesString).append(")\n")
                    .append(")\n");
        }

        //check star and votes and get query from review
        boolean from = isValidDateFormat(fromTextField.getText());
        boolean to = isValidDateFormat(fromTextField.getText());
//        boolean stars = isNumeric(starTextField.getText()) && starComboBox.getSelectedIndex() > 0;
//        boolean votes = isNumeric(votesTextField.getText()) && votesComboBox.getSelectedIndex() > 0;
        if ((from && to) || stars || votes) {
            query.append("\nAND b.business_id IN (\n")
                    .append("  SELECT r.business_id\n")
                    .append("  FROM Review r\n")
                    .append("  WHERE r.business_id = r.business_id\n");
            if (from && to) {
                query.append("          AND r.review_date >= '").append(getDate(fromTextField.getText())).append("' AND r.review_date <= '").append(getDate(toTextField.getText())).append("'\n");
            }
//            if (stars) {
//                query.append("          AND r.stars ").append(starComboBox.getSelectedItem().toString()).append(" ").append(starTextField.getText()).append("\n");
//            }
//            if (votes) {
//                query.append("          AND r.votes ").append(votesComboBox.getSelectedItem().toString()).append(" ").append(votesTextField.getText()).append("\n");
//            }
            query.append(")\n");
        }

        System.out.println("DEBUG==============business query: \n" + query.toString());
        return query;
    }

    private StringBuilder getUserQueryString() {
        StringBuilder query = new StringBuilder();
        if (userSearchForComboBox.getSelectedIndex() < 1) {
            return query;
        }
        boolean memberSince = isValidDateFormat(memberSinceTextField.getText());
        boolean reviewCount = isNumeric(reviewCountTextField.getText()) && reviewCountComboBox.getSelectedIndex() > 0;
        boolean numberOfFriends = isNumeric(numberOfFriendsTextField.getText()) && numberOfFriendsComboBox.getSelectedIndex() > 0;
        boolean averageStars = isNumeric(averageStarsTextField.getText()) && averageStarsComboBox.getSelectedIndex() > 0;
        boolean numberOfVotes = isNumeric(numberOfVotesTextField.getText()) && numberOfVotesComboBox.getSelectedIndex() > 0;
        String selector = userSearchForComboBox.getSelectedItem().toString();
        query.append("SELECT y.name, y.user_id, y.yelping_since, y.review_count, y.friend_count, y.average_stars, y.votes\n")
                .append("FROM YelpUser y\n");
        query.append("WHERE y.name = y.name");
        if (memberSince || reviewCount || numberOfFriends || averageStars || numberOfVotes) {
            //check Review Count and get value
            if (memberSince) {
                query.append(" ").append(selector)
                        .append(" y.yelping_since >= '").append(getDate(memberSinceTextField.getText())).append("'");
            }
            if (reviewCount) {
                query.append(" ").append(selector)
                        .append(" y.review_count ").append(reviewCountComboBox.getSelectedItem()).append(" ").append(reviewCountTextField.getText());
            }
            if (numberOfFriends) {
                query.append(" ").append(selector)
                        .append(" y.friend_count ").append(numberOfFriendsComboBox.getSelectedItem()).append(" ").append(numberOfFriendsTextField.getText());
            }
            if (averageStars) {
                query.append(" ").append(selector)
                        .append(" y.average_stars ").append(averageStarsComboBox.getSelectedItem()).append(" ").append(averageStarsTextField.getText());
            }
            if (numberOfVotes) {
                query.append(" ").append(selector)
                        .append(" y.votes ").append(numberOfVotesComboBox.getSelectedItem()).append(" ").append(numberOfVotesTextField.getText());
            }
        }

        //check star and votes and get query from review
        boolean from = isValidDateFormat(fromTextField.getText());
        boolean to = isValidDateFormat(toTextField.getText());
        boolean star = isNumeric(starTextField.getText()) && starComboBox.getSelectedIndex() > 0;
        boolean votes = isNumeric(votesTextField.getText()) && votesComboBox.getSelectedIndex() > 0;
        if ((from && to) || star || votes) {
            query.append("\n\nAND y.user_id IN (\n")
                    .append("  SELECT r.user_id\n")
                    .append("  FROM Review r\n")
                    .append("  WHERE r.business_id = r.business_id\n");
            if (from && to) {
                query.append("          AND r.review_date >= '").append(fromTextField.getText()).append("' AND r.review_date <= '").append(toTextField.getText()).append("'");
            }
            if (star) {
                query.append("          AND r.stars " + starComboBox.getSelectedItem().toString() + " " + starTextField.getText() + "\n");
            }
            if (votes) {
                query.append("          AND r.votes" + votesComboBox.getSelectedItem().toString() + " " + votesTextField.getText() + "\n");
            }
            query.append(")\n");
        }
        System.out.println("DEBUG============== user query: \n" + query.toString());
        return query;
    }

    private void showReview(String id) throws SQLException, ClassNotFoundException {
        System.out.println("Get review information...");
        JFrame reviewFrame = new JFrame("Review");
        reviewFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        reviewFrame.setSize(500, 600);
        reviewFrame.setLayout(new GridLayout(1, 1));
        reviewFrame.setVisible(true);
        TableModel dataModel = new DefaultTableModel();
        JTable reviewTable = new JTable(dataModel);
        JScrollPane scrollpane = new JScrollPane(reviewTable);

        DefaultTableModel defaultTableModel;
        String[][] data;
        try (Connection connection = Populate.getConnect();) {
            StringBuilder query = new StringBuilder();
            PreparedStatement preparedStatement;
            ResultSet rs;
            query.append("SELECT y.name, r.business_id, r.user_id, r.review_date, r.stars, r.votes\n")
                    .append("FROM Review r, YelpUser y\n")
                    .append("WHERE r.user_id = y.user_id");
            if (businessRadioButton.isSelected()) {
                query.append(" AND r.business_id = '").append(id).append("'\n");
            } else if (userRadioButton.isSelected()) {
                query.append(" AND r.user_id = '").append(id).append("'\n");
            }
            if (isValidDateFormat(fromTextField.getText()) && isValidDateFormat(toTextField.getText())) {
                query.append(" AND r.review_date >= '").append(getDate(fromTextField.getText())).append("' AND r.review_date <= '").append(getDate(toTextField.getText())).append("'");
            }
            if (isNumeric(starTextField.getText()) && starComboBox.getSelectedIndex() > 0) {
                query.append(" AND r.stars ").append(starComboBox.getSelectedItem().toString()).append(" ").append(starTextField.getText()).append("\n");
            }
            if (isNumeric(votesTextField.getText()) && votesComboBox.getSelectedIndex() > 0) {
                query.append(" AND r.votes ").append(votesComboBox.getSelectedItem().toString()).append(" ").append(votesTextField.getText()).append("\n");
            }
            System.out.println("DEBUG================= review query: \n" + query.toString());
            preparedStatement = connection.prepareStatement(query.toString(), ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            rs = preparedStatement.executeQuery();

            rs.last();
            ResultSetMetaData rsmd = rs.getMetaData();
            int rowCount = rs.getRow();
            int columnCount = rsmd.getColumnCount();
            data = new String[rowCount][columnCount];
            String[] columnNames = new String[columnCount];

            // get column names
            for (int i = 1; i <= columnCount; i++) {
                columnNames[i - 1] = rsmd.getColumnName(i);
            }

            rs.beforeFirst();
            for (int i = 0; i < rowCount; i++) {
                if (rs.next()) {
                    for (int j = 1; j <= columnCount; j++) {
                        data[i][j - 1] = rs.getString(j);
                    }
                }
            }
            rs.close();
            preparedStatement.close();
            defaultTableModel = new DefaultTableModel(data, columnNames);
            reviewTable.setModel(defaultTableModel);

            reviewFrame.add(scrollpane);
        }
    }

    private boolean isNumeric(String s) {
        if (s == null || s.length() == 0) {
            return false;
        }
        try {
            Integer num = Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return false;
        }
        return true;
    }

    private static String getDate(String inDate) {
        SimpleDateFormat formater = new SimpleDateFormat(DATE_FORMAT);
        DateFormat format = new SimpleDateFormat(DATE_FORMAT, Locale.ENGLISH);
        Date date = null;
        try {
            date = format.parse(inDate);
        } catch (ParseException ex) {
            Logger.getLogger(hw3.class.getName()).log(Level.SEVERE, null, ex);
        }
        return formater.format(date);
    }

    private static boolean isValidDateFormat(String inDate) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
        dateFormat.setLenient(false);
        try {
            dateFormat.parse(inDate.trim());
        } catch (ParseException pe) {
            return false;
        }
        return true;
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
//            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
//                if ("Nimbus".equals(info.getName())) {
//                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
//                    break;
//                }
//            }
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(hw3.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            Logger.getLogger(hw3.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            Logger.getLogger(hw3.class.getName()).log(Level.SEVERE, null, ex);
        } catch (UnsupportedLookAndFeelException ex) {
            Logger.getLogger(hw3.class.getName()).log(Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new hw3().setVisible(true);
            }
        });
    }

//    private JXDatePicker datePicker = new JXDatePicker(System.currentTimeMillis());

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel Selection;
    private javax.swing.JPanel View;
    private javax.swing.JLabel attributeLabel;
    private javax.swing.JPanel attributeListPanel;
    private javax.swing.JPanel attributePanel;
    private javax.swing.JScrollPane attributeScrollPane;
    private javax.swing.JPanel businessPanel;
    private javax.swing.JPanel categoriesPanel;
    private javax.swing.JLabel categoryLabel;
    private javax.swing.JComboBox<String> day;
    private javax.swing.JComboBox<String> from;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JComboBox<String> location;
    private javax.swing.JPanel mCategoryListPanel;
    private javax.swing.JPanel mainCategoryPanel;
    private javax.swing.JScrollPane mainCategoryScrollPane;
    private javax.swing.JButton queryButton;
    private javax.swing.JButton queryButton1;
    private javax.swing.JPanel queryPanel;
    private javax.swing.JTable resultTable;
    private javax.swing.JPanel sCategoryListPanel;
    private javax.swing.JLabel subCategoryLabel;
    private javax.swing.JPanel subCategoryPanel;
    private javax.swing.JScrollPane subCategoryScrollPane;
    private javax.swing.JComboBox<String> to;
    // End of variables declaration//GEN-END:variables
}
