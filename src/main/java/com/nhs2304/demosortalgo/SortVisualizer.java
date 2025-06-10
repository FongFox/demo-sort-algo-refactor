// SortVisualizer.java - Full version with TableView, SortStep, and getPseudoCode

package com.nhs2304.demosortalgo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nhs2304.demosortalgo.helper.AlgorithmResources;
import com.nhs2304.demosortalgo.model.HistoryEntry;
import com.nhs2304.demosortalgo.model.HistoryManager;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

public class SortVisualizer extends Application {
    private int lastIValue = -1;
    private int[] array;
    private int swapCount = 0;
    private Canvas canvas;
    private GraphicsContext gc;
    private boolean isSorting = false;
    private boolean stepMode = false;
    private int stepIndex = 0;
    private List<SortStep> steps = new ArrayList<>();
    private List<int[]> previousStates = new ArrayList<>();
    private Set<Integer> highlightIndices = new HashSet<>();
    private int[] originalArray;
    private TextField inputField;
    private ProgressIndicator loadingIndicator;
    private TextArea pseudoCodeArea;
    private ComboBox<String> algoCombo;
    private Slider speedSlider;
    private Label speedLabel;
    private TextField speedInput;
    private Label statusLabel;
    private long stepStartTime;
    private TableView<HistoryEntry> historyView = new TableView<>();
    private List<HistoryEntry> historyList = new ArrayList<>();
    private volatile boolean isPaused = false;

    private enum StepModeAlgorithm {NONE, BUBBLE, SELECTION, QUICK}

    private StepModeAlgorithm stepAlgo = StepModeAlgorithm.NONE;
    private volatile boolean isStopped = false;
    private TextArea algorithmInfoArea;

    /// === Entry Point ===

    public static void main(String[] args) {
        launch();
    }

    /// === Sort Step DTO ===
    private static class SortStep {
        int i, j;
        boolean swap;
        int line;

        SortStep(int i, int j, boolean swap, int line) {
            this.i = i;
            this.j = j;
            this.swap = swap;
            this.line = line;
        }

        void apply(int[] arr) {
            if (swap) {
                int temp = arr[i];
                arr[i] = arr[j];
                arr[j] = temp;
            }
        }

        int getLine() {
            return line;
        }
    }

    /// === Utility Methods ===

    private void showAlert(String title, String msg) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(title);
            alert.setContentText(msg);
            alert.showAndWait();
        });
    }

    private void updateStatus(String msg) {
        Platform.runLater(() -> statusLabel.setText("Status: " + msg));
    }

    private void sleep(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ignored) {
        }
    }

    private void drawArray() {
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        if (array == null || array.length == 0) {
            updateStatus("No data to sort!");
            return;
        }


        double width = canvas.getWidth();
        double height = canvas.getHeight();
        double heightScale = height / 100.0;

        for (int i = 0; i < array.length; i++) {
            double x1 = (width * i) / array.length;
            double x2 = (width * (i + 1)) / array.length;
            double barWidth = x2 - x1;  // Tính trực tiếp khoảng cách 2 vạch
            double h = array[i] * heightScale;
            double y = height - h;

            gc.setFill(highlightIndices.contains(i) ? Color.ORANGERED : Color.CORNFLOWERBLUE);
            gc.fillRect(x1, y, barWidth, h);
            gc.setStroke(Color.BLACK);
            gc.strokeRect(x1, y, barWidth, h);
            gc.setFill(Color.BLACK);
            gc.fillText(String.valueOf(array[i]), x1 + barWidth / 4, y - 5);
        }
        highlightIndices.clear();
    }

    private void highlightPseudoCode(int lineNumber) {
        Platform.runLater(() -> {
            String[] lines = pseudoCodeArea.getText().split("\n");
            if (lineNumber >= 0 && lineNumber < lines.length) {
                pseudoCodeArea.deselect(); // Clear selection
                int start = 0;
                for (int i = 0; i < lineNumber; i++) {
                    start += lines[i].length() + 1; // +1 vì có dấu xuống dòng
                }
                pseudoCodeArea.selectRange(start, start + lines[lineNumber].length());
            }
        });
    }

    private void saveHistoryToFile() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.writerWithDefaultPrettyPrinter().writeValue(new File("history.json"), historyList);
        } catch (IOException e) {
            showAlert("Save Failed", e.getMessage());
        }
    }

    /// === Sorting Algorithms ===
    private void bubbleSort() {
        for (int i = 0; i < array.length - 1; i++) {
            lastIValue = i; // thêm dòng này
            highlightPseudoCode(0); // for i
            for (int j = 0; j < array.length - i - 1; j++) {
                highlightPseudoCode(1); // for j
                if (isStopped) return;
                while (isPaused) sleep(50);

                highlightPseudoCode(2); // if arr[j] > arr[j+1]
                if (array[j] > array[j + 1]) {
                    int temp = array[j];
                    array[j] = array[j + 1];
                    array[j + 1] = temp;
                    swapCount++;

                    highlightIndices.add(j);
                    highlightIndices.add(j + 1);

                    highlightPseudoCode(3); // swap(arr[j], arr[j+1])

                    Platform.runLater(this::drawArray);
                    sleep((int) speedSlider.getValue());
                }
            }
        }
    }

    private void selectionSort() {
        for (int i = 0; i < array.length - 1; i++) {
            highlightPseudoCode(0); // for i
            int min = i;
            for (int j = i + 1; j < array.length; j++) {
                highlightPseudoCode(1); // for j
                if (isStopped) return;
                while (isPaused) sleep(50);

                highlightPseudoCode(2); // if arr[j] < arr[min]
                if (array[j] < array[min]) {
                    min = j;
                }
            }
            if (min != i) {
                int temp = array[i];
                array[i] = array[min];
                array[min] = temp;
                swapCount++;

                highlightIndices.add(i);
                highlightIndices.add(min);

                highlightPseudoCode(5); // swap(arr[i], arr[min])

                Platform.runLater(this::drawArray);
                sleep((int) speedSlider.getValue());
            }
        }
    }

    private void quickSort(int low, int high) {
        if (low < high) {
            int pi = partition(low, high);

            if (isStopped) return;
            while (isPaused) sleep(50);

            quickSort(low, pi - 1);
            quickSort(pi + 1, high);
        }
    }

    private int partition(int low, int high) {
        highlightPseudoCode(7); // pivot = arr[high]
        int pivot = array[high];
        int i = (low - 1);

        for (int j = low; j < high; j++) {
            highlightPseudoCode(8); // for j
            if (isStopped) return i;
            while (isPaused) sleep(50);

            highlightPseudoCode(9); // if arr[j] < pivot
            if (array[j] < pivot) {
                i++;
                highlightPseudoCode(10); // swap arr[i], arr[j]

                int temp = array[i];
                array[i] = array[j];
                array[j] = temp;
                swapCount++;

                highlightIndices.add(i);
                highlightIndices.add(j);

                Platform.runLater(this::drawArray);
                sleep((int) speedSlider.getValue());
            }
        }

        highlightPseudoCode(11); // swap arr[i+1], arr[high]
        int temp = array[i + 1];
        array[i + 1] = array[high];
        array[high] = temp;
        swapCount++;

        highlightIndices.add(i + 1);
        highlightIndices.add(high);

        Platform.runLater(this::drawArray);
        sleep((int) speedSlider.getValue());

        return i + 1;
    }

    /// === Undefined Method ===
    private void generateBubbleSortSteps() {
        steps.clear();
        int[] tempArr = array.clone();
        for (int i = 0; i < tempArr.length - 1; i++) {
            for (int j = 0; j < tempArr.length - i - 1; j++) {
                steps.add(new SortStep(j, j + 1, false, 3));
                if (tempArr[j] > tempArr[j + 1]) {
                    steps.add(new SortStep(j, j + 1, true, 4));
                    int tmp = tempArr[j];
                    tempArr[j] = tempArr[j + 1];
                    tempArr[j + 1] = tmp;
                    swapCount++;
                }
            }
        }
    }

    private void generateSelectionSortSteps() {
        steps.clear();
        swapCount = 0;
        int[] tempArr = array.clone();

        for (int i = 0; i < tempArr.length - 1; i++) {
            int min = i;
            for (int j = i + 1; j < tempArr.length; j++) {
                steps.add(new SortStep(min, j, false, 2));
                if (tempArr[j] < tempArr[min]) {
                    min = j;
                }
            }

            if (min != i) {
                steps.add(new SortStep(i, min, true, 3));

                // 🔎 Ghi log để debug
                System.out.println("Swap at i=" + i + " ↔ min=" + min + " | " + tempArr[i] + " ↔ " + tempArr[min]);

                int tmp = tempArr[i];
                tempArr[i] = tempArr[min];
                tempArr[min] = tmp;

                swapCount++;
            }
        }
    }

    private void generateQuickSortSteps() {
        steps.clear();
        swapCount = 0;
        int[] tempArr = array.clone();
        quickSortSteps(tempArr, 0, tempArr.length - 1);
    }

    // Hàm đệ quy lưu các bước quick sort
    private void quickSortSteps(int[] arr, int low, int high) {
        if (low < high) {
            int pi = partitionSteps(arr, low, high);
            quickSortSteps(arr, low, pi - 1);
            quickSortSteps(arr, pi + 1, high);
        }
    }

    // Giống partition nhưng lưu SortStep
    private int partitionSteps(int[] arr, int low, int high) {
        int pivot = arr[high];
        int i = low - 1;

        steps.add(new SortStep(high, -1, false, 0)); // chọn pivot (line 0: pivot = arr[high])

        for (int j = low; j < high; j++) {
            steps.add(new SortStep(j, high, false, 1)); // compare arr[j] với pivot (line 1: for j)

            if (arr[j] < pivot) {
                i++;
                steps.add(new SortStep(i, j, true, 2)); // swap arr[i] <-> arr[j] (line 2)
                int tmp = arr[i];
                arr[i] = arr[j];
                arr[j] = tmp;
                swapCount++;
            }
        }

        steps.add(new SortStep(i + 1, high, true, 3)); // swap arr[i+1] <-> arr[high] (line 3)
        int tmp = arr[i + 1];
        arr[i + 1] = arr[high];
        arr[high] = tmp;
        swapCount++;

        return i + 1;
    }

    /// === JavaFX start() ===
    @Override
    public void start(Stage primaryStage) {
        // Đọc lịch sử đã lưu (nếu có)
        historyList = HistoryManager.loadHistory("history.json");
        historyView.getItems().addAll(historyList);

        Button runBtn = new Button("Run");
        Button pauseBtn = new Button("Pause");
        Button nextBtn = new Button("Next");
        Button backBtn = new Button("Back");
        Button stopBtn = new Button("Stop");
        Button randomBtn = new Button("Random");

        inputField = new TextField();
        inputField.setPromptText("Enter numbers (e.g. 5,3,8,1)");

        loadingIndicator = new ProgressIndicator();
        loadingIndicator.setVisible(false);
        loadingIndicator.setPrefSize(25, 25);

        speedSlider = new Slider(100, 2000, 500);
        speedSlider.setPrefWidth(300);
        speedSlider.setShowTickLabels(true);    // Hiển thị số
        speedSlider.setShowTickMarks(true);     // Hiển thị vạch chia
        speedSlider.setMajorTickUnit(500);      // Mỗi 500ms có 1 vạch lớn (500 - 1000 - 1500 - 2000)
        speedSlider.setMinorTickCount(4);       // 4 vạch phụ chia đều
        speedSlider.setBlockIncrement(100);     // Mỗi lần kéo hoặc bấm nhảy 100ms
        speedSlider.setSnapToTicks(true);       // Dính vào đúng vạch chia


        speedSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            int value = newVal.intValue();
            speedInput.setText(String.valueOf(value));
            speedLabel.setText("Speed: " + value + "ms");
        });


        speedLabel = new Label("Speed: 500ms");
        speedInput = new TextField("500");
        speedInput.setPrefWidth(60);
        speedInput.textProperty().addListener((obs, oldText, newText) -> {
            try {
                int value = Integer.parseInt(newText.trim());
                if (value >= 100 && value <= 2000) { // Chỉ cho phép nhập trong khoảng 100–2000
                    speedSlider.setValue(value);
                    speedLabel.setText("Speed: " + value + "ms");
                }
            } catch (NumberFormatException ignored) {
            }
        });


        algoCombo = new ComboBox<>();
        algoCombo.getItems().addAll("Bubble Sort", "Selection Sort", "Quick Sort");
        algoCombo.setValue("Bubble Sort");
        algoCombo.setOnAction(e -> {
            pseudoCodeArea.setText(AlgorithmResources.getPseudoCode(algoCombo.getValue()));
            algorithmInfoArea.setText(AlgorithmResources.getAlgorithmInfo(algoCombo.getValue()));
            stepMode = false; // 🛡️ reset step-by-step mode khi đổi thuật toán
        });


        HBox controls = new HBox(10,
                new Label("Algorithm:"), algoCombo,
                runBtn, pauseBtn, stopBtn,
                speedLabel, speedInput, speedSlider, // Đưa qua đây
                backBtn, nextBtn,
                inputField, randomBtn,
                loadingIndicator
        );


        controls.setStyle("-fx-padding: 10; -fx-alignment: center;");

        canvas = new Canvas(1000, 500);
        gc = canvas.getGraphicsContext2D();

        pseudoCodeArea = new TextArea();
        algorithmInfoArea = new TextArea();
        VBox rightSide = new VBox();
        rightSide.getChildren().addAll(pseudoCodeArea, algorithmInfoArea);
        VBox.setVgrow(pseudoCodeArea, Priority.ALWAYS);
        VBox.setVgrow(algorithmInfoArea, Priority.ALWAYS);
        algorithmInfoArea.setStyle("-fx-font-size: 16px; -fx-font-family: 'Segoe UI'; -fx-text-fill: black;");
        VBox.setMargin(algorithmInfoArea, new Insets(10, 0, 0, 0)); // Đẩy lên 10px
        pseudoCodeArea.setStyle("-fx-font-size: 16px; -fx-font-family: 'Consolas'; -fx-text-fill: black;");

        algorithmInfoArea.setEditable(false);
        algorithmInfoArea.setWrapText(true);
        algorithmInfoArea.setPrefHeight(220); // hoặc 200 tùy ông

        pseudoCodeArea.setEditable(false);
        pseudoCodeArea.setWrapText(true);
        pseudoCodeArea.setPrefWidth(400);


        TableColumn<HistoryEntry, String> algoCol = new TableColumn<>("Algorithm");
        algoCol.setCellValueFactory(new PropertyValueFactory<>("algorithm"));

        TableColumn<HistoryEntry, String> inputCol = new TableColumn<>("Input");
        inputCol.setCellValueFactory(new PropertyValueFactory<>("inputArray"));
        inputCol.setPrefWidth(250); // hoặc 300 nếu muốn

        inputCol.setCellFactory(tc -> {
            TableCell<HistoryEntry, String> cell = new TableCell<>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                        setTooltip(null);
                    } else {
                        setText(item);
                        Tooltip tooltip = new Tooltip(item);
                        setTooltip(tooltip);
                    }
                }
            };
            return cell;
        });


        TableColumn<HistoryEntry, String> outputCol = new TableColumn<>("Output");
        outputCol.setCellValueFactory(new PropertyValueFactory<>("outputArray"));
        outputCol.setPrefWidth(250);
        outputCol.setCellFactory(tc -> {
            TableCell<HistoryEntry, String> cell = new TableCell<>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                        setTooltip(null);
                    } else {
                        setText(item);
                        Tooltip tooltip = new Tooltip(item);
                        setTooltip(tooltip);
                    }
                }
            };
            return cell;
        });


        TableColumn<HistoryEntry, String> timeCol = new TableColumn<>("Start Time");
        timeCol.setCellValueFactory(new PropertyValueFactory<>("startTime"));

        TableColumn<HistoryEntry, String> durationCol = new TableColumn<>("Duration");
        durationCol.setCellValueFactory(new PropertyValueFactory<>("formattedDuration"));

        TableColumn<HistoryEntry, Integer> swapCol = new TableColumn<>("Swaps");
        TableColumn<HistoryEntry, Integer> iCol = new TableColumn<>("i");
        iCol.setCellValueFactory(new PropertyValueFactory<>("iValue"));

        swapCol.setCellValueFactory(new PropertyValueFactory<>("swapCount"));

        historyView.getColumns().addAll(
                algoCol,
                inputCol,
                iCol,           // 👈 đưa i ngay sau input
                outputCol,
                timeCol,
                durationCol,
                swapCol
        );

        VBox leftSide = new VBox(canvas, new Label("History"), historyView);
        canvas.widthProperty().bind(leftSide.widthProperty());
        canvas.heightProperty().bind(new SimpleDoubleProperty(500));

        SplitPane splitPane = new SplitPane(leftSide, rightSide); // Đổi pseudoCodeArea thành rightSide
        splitPane.setDividerPositions(0.7);

        statusLabel = new Label("Ready");
        HBox statusBar = new HBox(statusLabel);
        statusBar.setStyle("-fx-padding: 5; -fx-background-color: #f0f0f0;");
        statusBar.setAlignment(Pos.CENTER_LEFT);

        BorderPane root = new BorderPane();

        root.setTop(controls);
        root.setCenter(splitPane);
        root.setBottom(statusBar);

        primaryStage.setScene(new Scene(root, 1400, 600));
        primaryStage.setTitle("Sort Visualizer - History TableView Version");
        primaryStage.show();

        inputField.textProperty().addListener((obs, oldText, newText) -> {
            try {
                array = Arrays.stream(newText.split(","))
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .mapToInt(Integer::parseInt)
                        .toArray();

                stepMode = false; // 🛡️ reset step-by-step nếu nhập input mới
                drawArray();
            } catch (Exception ignored) {
            }
        });


        randomBtn.setOnAction(e -> {
            TextInputDialog dialog = new TextInputDialog("10");
            dialog.setTitle("Random Array");
            dialog.setHeaderText("Random Number Generator");
            dialog.setContentText("Enter number of elements (1-100):");

            Optional<String> result = dialog.showAndWait();
            result.ifPresent(input -> {
                try {
                    int count = Integer.parseInt(input.trim());
                    if (count <= 0 || count > 100) {
                        showAlert("Invalid Input", "Please enter a number between 1 and 100.");
                        return;
                    }
                    Random rnd = new Random();
                    array = new int[count];
                    for (int i = 0; i < count; i++) {
                        array[i] = rnd.nextInt(100); // Random từ 0 đến 99
                    }
                    inputField.setText(Arrays.toString(array).replaceAll("[\\[\\]]", ""));
                    drawArray();
                } catch (NumberFormatException ex) {
                    showAlert("Invalid Input", "Please enter a valid integer.");
                }
            });
        });

        runBtn.setOnAction(e -> {
            if (array != null && array.length > 0) {
                isSorting = true;
                isStopped = false;
                isPaused = false;
                updateStatus("Running");
                loadingIndicator.setVisible(true);
                pseudoCodeArea.setText(AlgorithmResources.getPseudoCode(algoCombo.getValue()));
                algorithmInfoArea.setText(AlgorithmResources.getAlgorithmInfo(algoCombo.getValue()));

                int[] original = array.clone();
                long startTimeMillis = System.currentTimeMillis();
                String startTimeStr = LocalTime.now().withNano(0).toString();

                // ➔ Bọc toàn bộ sorting vào 1 Thread mới
                new Thread(() -> {
                    switch (algoCombo.getValue()) {
                        case "Bubble Sort":
                            bubbleSort();
                            break;
                        case "Selection Sort":
                            selectionSort();
                            break;
                        case "Quick Sort":
                            quickSort(0, array.length - 1);
                            break;
                        default:
                            updateStatus("Unknown Algorithm");
                            return;
                    }

                    Platform.runLater(() -> {
                        long duration = System.currentTimeMillis() - startTimeMillis;
                        HistoryEntry entry = new HistoryEntry(
                                algoCombo.getValue(),
                                Arrays.toString(original),
                                Arrays.toString(array),
                                duration,
                                swapCount,
                                startTimeStr,
                                lastIValue // ← thêm dòng này
                        );

                        historyList.add(entry);
                        historyView.getItems().add(entry);
                        updateStatus("Done in " + duration + " ms");
                        saveHistoryToFile();
                        loadingIndicator.setVisible(false);
                    });
                }).start(); // ➔ Bắt buộc .start() ngay sau new Thread
            } else {
                updateStatus("No data to sort!");
            }
        });

        // Thiết lập sự kiện cho nút pause
        pauseBtn.setOnAction(e -> {
            if (isSorting) {
                if (isPaused) {
                    isPaused = false;
                    updateStatus("Running");
                    pauseBtn.setText("Pause");  // Đổi label nút về Pause
                } else {
                    isPaused = true;
                    updateStatus("Paused");
                    pauseBtn.setText("Resume"); // Đổi label nút thành Resume
                }
            }
        });

        // Thiết lập sự kiện cho nút stop
        stopBtn.setOnAction(e -> {
            if (isSorting) {
                isStopped = true;
                updateStatus("Stopped");
                loadingIndicator.setVisible(false);
            }
        });

        // Thêm các sự kiện và giao diện còn lại như trước
        nextBtn.setOnAction(e -> {
            if (!stepMode) {
                if (array == null || array.length == 0) return;
                stepStartTime = System.currentTimeMillis();
                stepMode = true;
                historyView.getItems().clear();   // XÓA lịch sử hiển thị cũ
                historyList.clear();              // Nếu muốn xóa luôn khỏi memory (file vẫn còn)

                stepMode = true;
                originalArray = array.clone();
                swapCount = 0;
                steps.clear();
                previousStates.clear();
                stepIndex = 0;
                stepAlgo = switch (algoCombo.getValue()) {
                    case "Bubble Sort" -> {
                        generateBubbleSortSteps();
                        yield StepModeAlgorithm.BUBBLE;
                    }
                    case "Selection Sort" -> {
                        generateSelectionSortSteps();
                        yield StepModeAlgorithm.SELECTION;
                    }
                    case "Quick Sort" -> {
                        generateQuickSortSteps();
                        yield StepModeAlgorithm.QUICK;
                    }
                    default -> StepModeAlgorithm.NONE;
                };
                pseudoCodeArea.setText(AlgorithmResources.getPseudoCode(algoCombo.getValue()));
                algorithmInfoArea.setText(AlgorithmResources.getAlgorithmInfo(algoCombo.getValue()));

                updateStatus("Step-by-step started");
            } else {
                if (stepIndex < steps.size()) {
                    previousStates.add(array.clone());
                    steps.get(stepIndex).apply(array);
                    highlightPseudoCode(steps.get(stepIndex).getLine());
                    highlightIndices.clear();
                    highlightIndices.add(steps.get(stepIndex).i);
                    highlightIndices.add(steps.get(stepIndex).j);

                    stepIndex++;
                    drawArray();
                    updateStatus("Step " + stepIndex + "/" + steps.size());
                } else {
                    updateStatus("Step-by-step complete");

                    long duration = System.currentTimeMillis() - stepStartTime;

                    HistoryEntry entry = new HistoryEntry(
                            algoCombo.getValue(),
                            Arrays.toString(originalArray),
                            Arrays.toString(array),
                            duration,
                            swapCount,
                            LocalDateTime.now().toString(),
                            lastIValue
                    );

                    historyList.add(entry);
                    historyView.getItems().add(entry);
                    saveHistoryToFile();
                }
            }


        });

        backBtn.setOnAction(e -> {
            if (stepMode && stepIndex > 0 && !previousStates.isEmpty()) {
                stepIndex--;
                array = previousStates.remove(previousStates.size() - 1);
                drawArray();
                highlightPseudoCode(steps.get(stepIndex).getLine());
                updateStatus("Back to step " + stepIndex);
            }
        });

        // Khi hoàn tất một lần chạy hoặc chế độ step-by-step
        // lưu lịch sử xuống file
        HistoryManager.saveHistory("history.json", historyList);
    }
}












































