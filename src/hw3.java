
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
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

    private void init() throws SQLException, ClassNotFoundException {
        System.out.println("....init....");
        // lisening to resultTable and get the information of review
        resultTable.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 1) {
                    JTable target = (JTable) e.getSource();
                    int row = target.getSelectedRow();
                    String id = resultTable.getModel().getValueAt(row, 0).toString();
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

        try (Connection connection = Populate.getConnect();) {
            StringBuilder sql = new StringBuilder();
            PreparedStatement preparedStatement;
            ResultSet rs;

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
                            System.out.println("DEBUG..........select mainCategories: " + mainCategoriesString.toString());
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
            value = value.replace("'", "''");
            if (first) {
                first = false;
            } else {
                sql.append(" AND ");
            }
            sql.append(name);
            sql.append(" = ");
            sql.append("'").append(value).append("'");

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
            value = value.replace("'", "''");
            if (first) {
                first = false;
            } else {
                sql.append(" , ");
            }
            sql.append("'").append(value).append("'");
        }
        sql.append(")");
        return sql.toString();
    }

    private String getQuery(String name, HashSet<String> values) {
        if (jRadioButtonOr.isSelected()) {
            return getOrQuery(name, values);
        } else {
            return getAndQuery(name, values);
        }
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
            System.out.println("DEBUG............. select subCategories: " + sql.toString() + "\n");
            allSubCategoriesSet.clear();
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
                        System.out.println("DEBUG.......... select subCategories: " + subCategoriesString.toString() + "\n");
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
            System.out.println("DEBUG.......... select attributes:\n" + sql.toString());
            preparedStatement = connection.prepareStatement(sql.toString());
            rs = preparedStatement.executeQuery();
            allAttributesSet.clear();
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
                        System.out.println("DEBUG.......... attributes: " + attributesString.toString());
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
        DefaultTableModel defaultTableModel;
        String[][] data;

        try (Connection connection = Populate.getConnect();) {
            PreparedStatement preparedStatement;
            ResultSet rs;
            String query = getBusinessQueryString().toString();

            preparedStatement = connection.prepareStatement(query, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            rs = preparedStatement.executeQuery();

            rs.last();
            ResultSetMetaData rsmd = rs.getMetaData();
            int rowCount = rs.getRow();
            int columnCount = rsmd.getColumnCount();
            data = new String[rowCount][columnCount];
            String[] columnNames = new String[columnCount];

            SortedSet<String> cities = new TreeSet<String>();

            // get column names
            for (int i = 1; i <= columnCount; i++) {
                columnNames[i - 1] = rsmd.getColumnName(i);
            }

            rs.beforeFirst();
            for (int i = 0; i < rowCount; i++) {
                if (rs.next()) {
                    for (int j = 1; j <= columnCount; j++) {
                        if (rsmd.getColumnName(j).equals("CITY")) {
                            cities.add(rs.getString(j));
                        }
                        data[i][j - 1] = rs.getString(j);
                    }
                }
            }

            // update city selection.
            location.removeAllItems();
            location.addItem("");
            for (String city : cities) {
                location.addItem(city);
            }
            location.updateUI();

            rs.close();
            preparedStatement.close();
            defaultTableModel = new DefaultTableModel(data, columnNames);
            resultTable.removeAll();
            resultTable.setModel(defaultTableModel);
        } catch (SQLException ex) {
            Logger.getLogger(hw3.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(hw3.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        andor = new javax.swing.ButtonGroup();
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
        jRadioButtonOr = new javax.swing.JRadioButton();
        jRadioButtonAnd = new javax.swing.JRadioButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setBackground(new java.awt.Color(0, 0, 153));

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
        queryButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                queryButtonActionPerformed(evt);
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

        day.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] {
            "",
            "Sunday",
            "Monday",
            "Tuesday",
            "Wednesday",
            "Thursday",
            "Friday",
            "Saturday" }));
day.addActionListener(new java.awt.event.ActionListener() {
    public void actionPerformed(java.awt.event.ActionEvent evt) {
        dayActionPerformed(evt);
    }
    });

    jLabel1.setText("Day of the week:");

    jLabel2.setText("From:");

    from.setModel(new javax.swing.DefaultComboBoxModel<>(
        new String[] { "",
            "00:00" ,
            "01:00" ,
            "02:00" ,
            "03:00" ,
            "04:00" ,
            "05:00" ,
            "06:00" ,
            "07:00" ,
            "08:00" ,
            "09:00" ,
            "10:00" ,
            "11:00" ,
            "12:00" ,
            "13:00" ,
            "14:00" ,
            "15:00" ,
            "16:00" ,
            "17:00" ,
            "19:00" ,
            "20:00" ,
            "21:00" ,
            "22:00" ,
            "23:00" ,
            "24:00"  }));
from.addActionListener(new java.awt.event.ActionListener() {
    public void actionPerformed(java.awt.event.ActionEvent evt) {
        fromActionPerformed(evt);
    }
    });

    jLabel3.setText("To:");

    to.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "",
        "00:00" ,
        "01:00" ,
        "02:00" ,
        "03:00" ,
        "04:00" ,
        "05:00" ,
        "06:00" ,
        "07:00" ,
        "08:00" ,
        "09:00" ,
        "10:00" ,
        "11:00" ,
        "12:00" ,
        "13:00" ,
        "14:00" ,
        "15:00" ,
        "16:00" ,
        "17:00" ,
        "19:00" ,
        "20:00" ,
        "21:00" ,
        "22:00" ,
        "23:00" ,
        "24:00"  }));

jLabel4.setText("Location:");

location.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "" }));

andor.add(jRadioButtonOr);
jRadioButtonOr.setSelected(true);
jRadioButtonOr.setText("OR");
jRadioButtonOr.addActionListener(new java.awt.event.ActionListener() {
public void actionPerformed(java.awt.event.ActionEvent evt) {
    jRadioButtonOrActionPerformed(evt);
    }
    });

    andor.add(jRadioButtonAnd);
    jRadioButtonAnd.setText("AND");
    jRadioButtonAnd.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            jRadioButtonAndActionPerformed(evt);
        }
    });

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
                .addComponent(jLabel3)
                .addComponent(to, javax.swing.GroupLayout.PREFERRED_SIZE, 89, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addGap(75, 75, 75)
            .addGroup(SelectionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(SelectionLayout.createSequentialGroup()
                    .addComponent(jLabel4, javax.swing.GroupLayout.DEFAULT_SIZE, 98, Short.MAX_VALUE)
                    .addGap(172, 172, 172))
                .addGroup(SelectionLayout.createSequentialGroup()
                    .addComponent(location, javax.swing.GroupLayout.PREFERRED_SIZE, 155, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
            .addGroup(SelectionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(jRadioButtonOr)
                .addComponent(jRadioButtonAnd))
            .addGap(42, 42, 42)
            .addComponent(queryButton, javax.swing.GroupLayout.PREFERRED_SIZE, 88, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(18, 18, 18)
            .addComponent(queryButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 88, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(32, 32, 32))
    );
    SelectionLayout.setVerticalGroup(
        SelectionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(SelectionLayout.createSequentialGroup()
            .addGap(16, 16, 16)
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
                        .addComponent(jLabel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jRadioButtonOr))
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                    .addGroup(SelectionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(day, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(from, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(to, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jRadioButtonAnd)
                        .addComponent(location, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGap(11, 11, 11))))
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

    private void queryButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_queryButtonActionPerformed
        updateItem();
    }//GEN-LAST:event_queryButtonActionPerformed

    private void jRadioButtonOrActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButtonOrActionPerformed
        System.out.println("Change to OR");
    }//GEN-LAST:event_jRadioButtonOrActionPerformed

    private void jRadioButtonAndActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButtonAndActionPerformed
        System.out.println("Change to AND");
    }//GEN-LAST:event_jRadioButtonAndActionPerformed

    private StringBuilder getBusinessQueryString() {
        StringBuilder query = new StringBuilder();

        //get query from category
        query.append("SELECT b.*, mc.MainCategory\n")
                .append("FROM Business b, MainCategory mc\n")
                .append("WHERE b.business_id = mc.business_id AND ").append(getQuery("mc.mainCategory", selectedMainCategoriesSet)).append("\n");

        //check subcategory and get query from subCategory
        query.append("\nAND b.business_id IN (\n")
                .append("  SELECT business_id\n")
                .append("  FROM SubCategory \n")
                .append("  WHERE ").append(getQuery("subCategory", selectedSubCategoriesSet)).append("\n")
                .append(")\n");

        //check subcategory and get query from attribute
        query.append("\nAND b.business_id IN (\n")
                .append("  SELECT business_id\n")
                .append("  FROM Attribute \n")
                .append("  WHERE ").append(getQuery("attribute", selectedAttributesSet)).append("\n")
                .append(")\n");

        String dayStr = day.getSelectedItem().toString();
        String fromStr = from.getSelectedItem().toString();
        String toStr = to.getSelectedItem().toString();
        String locationStr = location.getSelectedItem().toString();

        if (!locationStr.isEmpty()) {
            query.append("\nAND b.city = '")
                    .append(locationStr)
                    .append("'\n");
        }
        if (!dayStr.isEmpty() || !fromStr.isEmpty() || !toStr.isEmpty()) {
            query.append("\nAND b.business_id IN (\n")
                    .append("  SELECT business_id\n")
                    .append("  FROM Hours \n")
                    .append("  WHERE 1 = 1\n");
            if (!dayStr.isEmpty()) {
                query.append(" AND openDay = '").append(dayStr).append("'\n");
            }
            if (!fromStr.isEmpty()) {
                query.append(" AND closeTime > '").append(fromStr).append("'\n");
            }
            if (!toStr.isEmpty()) {
                query.append(" AND openTime < '").append(toStr).append("'\n");
            }
            query.append(")\n");
        }
        System.out.println("DEBUG.......... business query: \n" + query.toString());
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
            query.append("SELECT y.user_name, r.business_id, r.user_id, r.review_date, r.stars, r.text \n")
                    .append("FROM Review r, YelpUser y\n")
                    .append("WHERE r.user_id = y.user_id");
            query.append(" AND r.business_id = '").append(id).append("'\n");
            System.out.println("DEBUG.......... review query: \n" + query.toString());
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
                        try {
                            data[i][j - 1] = rs.getString(j);
                        } catch (SQLException e) {
                            Blob blob = rs.getBlob(j);
                            byte[] bdata = blob.getBytes(1, (int) blob.length());
                            data[i][j - 1] = new String(bdata);
                        }
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
    private javax.swing.ButtonGroup andor;
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
    private javax.swing.JRadioButton jRadioButtonAnd;
    private javax.swing.JRadioButton jRadioButtonOr;
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
