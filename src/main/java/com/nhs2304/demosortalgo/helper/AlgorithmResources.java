package com.nhs2304.demosortalgo.helper;

/**
 * Helper class that provides pseudocode and info text for each algorithm.
 */
public final class AlgorithmResources {
    private AlgorithmResources() {}

    public static String getPseudoCode(String algo) {
        return switch (algo) {
            case "Bubble Sort" -> """
                    for (int i = 0; i < n - 1; i++) {
                        for (int j = 0; j < n - i - 1; j++) {
                            if (arr[j] > arr[j + 1]) {
                                swap(arr[j], arr[j + 1]);
                            }
                        }
                    }
                    """;
            case "Selection Sort" -> """
                    for (int i = 0; i < n - 1; i++) {
                        int min = i;
                        for (int j = i + 1; j < n; j++) {
                            if (arr[j] < arr[min]) {
                                min = j;
                            }
                        }
                        swap(arr[i], arr[min]);
                    }
                    """;
            case "Quick Sort" -> """
                    quickSort(arr[], low, high):
                        if (low < high):
                            pi = partition(arr, low, high)
                            quickSort(arr, low, pi - 1)
                            quickSort(arr, pi + 1, high)

                    partition(arr[], low, high):
                        pivot = arr[high]
                        i = low - 1
                        for j = low to high-1:
                            if arr[j] < pivot:
                                i++
                                swap arr[i] with arr[j]
                        swap arr[i+1] with arr[high]
                        return (i + 1)
                    """;
            default -> "Thông tin chưa có.";
        };
    }

    public static String getAlgorithmInfo(String algorithm) {
        return switch (algorithm) {
            case "Bubble Sort" -> """
                    ➔ Bubble Sort
                    - Người phát triển: Edward F. Moore (~1950)
                    - Độ phức tạp thời gian:
                      + Trung bình: O(n²)
                      + Tệ nhất: O(n²)
                      + Tốt nhất: O(n) (khi dãy đã sorted)
                    - Độ phức tạp bộ nhớ: O(1) (in-place sorting)
                    - Tính chất:
                      + Ổn định (stable sort: YES)
                      + So sánh cặp kề và hoán đổi nếu sai thứ tự.
                    - Ứng dụng:
                      + Dạy sorting cơ bản.
                      + Tình huống mảng nhỏ hoặc đã gần sorted.
                    """;
            case "Selection Sort" -> """
                    ➔ Selection Sort
                    - Người phát triển: Donald Shell (~1950s)
                    - Độ phức tạp thời gian:
                      + Trung bình: O(n²)
                      + Tệ nhất: O(n²)
                      + Tốt nhất: O(n²)
                    - Độ phức tạp bộ nhớ: O(1) (in-place sorting)
                    - Tính chất:
                      + Không ổn định (stable sort: NO)
                      + Tìm phần tử nhỏ nhất và đưa lên đầu.
                    - Ứng dụng:
                      + Dùng trong môi trường bộ nhớ giới hạn.
                      + Dạy thuật toán cơ bản.
                    """;
            case "Quick Sort" -> """
                    ➔ Quick Sort
                    - Người phát triển: Tony Hoare (1960)
                    - Độ phức tạp thời gian:
                      + Trung bình: O(n log n)
                      + Tệ nhất: O(n²) (nếu chọn pivot không tốt)
                      + Tốt nhất: O(n log n)
                    - Độ phức tạp bộ nhớ: O(log n) (stack recursion)
                    - Tính chất:
                      + Không ổn định (stable sort: NO)
                      + Chia để trị: chọn pivot, chia mảng thành 2 phần nhỏ hơn và lớn hơn pivot.
                      + Hiệu quả cao với mảng lớn, nhưng dễ bị O(n²) nếu pivot không cân bằng.
                    - Ứng dụng:
                      + Sorting cực nhanh trên các hệ thống thực tế.
                      + Được dùng làm chuẩn sorting trong nhiều thư viện chuẩn (ví dụ Java, C++ STL, Python).
                    """;
            default -> "Thông tin chưa có.";
        };
    }
}
