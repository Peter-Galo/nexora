import { Injectable, signal } from '@angular/core';
import { Observable } from 'rxjs';

/**
 * Command execution result interface
 */
export interface CommandResult<T = any> {
  success: boolean;
  data?: T;
  error?: string;
  timestamp: Date;
}

/**
 * Command execution context interface
 */
export interface CommandContext {
  [key: string]: any;
}

/**
 * Base Command interface implementing Command Pattern
 */
export interface ICommand<T = any> {
  execute(context?: CommandContext): Observable<CommandResult<T>>;
  undo?(): Observable<CommandResult<void>>;
  canExecute(context?: CommandContext): boolean;
  readonly name: string;
  readonly description?: string;
}

/**
 * Abstract Base Command class
 * Implements the Command pattern for encapsulating user actions
 */
export abstract class BaseCommand<T = any> implements ICommand<T> {
  // Command state signals
  private readonly _isExecuting = signal<boolean>(false);
  private readonly _lastResult = signal<CommandResult<T> | null>(null);
  private readonly _executionCount = signal<number>(0);

  // Public readonly signals
  readonly isExecuting = this._isExecuting.asReadonly();
  readonly lastResult = this._lastResult.asReadonly();
  readonly executionCount = this._executionCount.asReadonly();

  // Command metadata
  abstract readonly name: string;
  readonly description?: string;

  // Execution history
  private readonly executionHistory: CommandResult<T>[] = [];
  private readonly maxHistorySize = 10;

  /**
   * Execute the command with optional context
   */
  execute(context?: CommandContext): Observable<CommandResult<T>> {
    if (!this.canExecute(context)) {
      return new Observable(observer => {
        const result: CommandResult<T> = {
          success: false,
          error: `Command ${this.name} cannot be executed`,
          timestamp: new Date()
        };
        observer.next(result);
        observer.complete();
      });
    }

    this._isExecuting.set(true);

    return new Observable(observer => {
      this.doExecute(context)
        .subscribe({
          next: (result) => {
            this.handleExecutionResult(result);
            observer.next(result);
            observer.complete();
          },
          error: (error) => {
            const errorResult: CommandResult<T> = {
              success: false,
              error: error.message || 'Command execution failed',
              timestamp: new Date()
            };
            this.handleExecutionResult(errorResult);
            observer.next(errorResult);
            observer.complete();
          }
        });
    });
  }

  /**
   * Abstract method to be implemented by concrete commands
   */
  protected abstract doExecute(context?: CommandContext): Observable<CommandResult<T>>;

  /**
   * Undo the command (optional implementation)
   */
  undo(): Observable<CommandResult<void>> {
    return new Observable(observer => {
      const result: CommandResult<void> = {
        success: false,
        error: `Undo not implemented for command ${this.name}`,
        timestamp: new Date()
      };
      observer.next(result);
      observer.complete();
    });
  }

  /**
   * Check if the command can be executed
   */
  canExecute(context?: CommandContext): boolean {
    return !this._isExecuting();
  }

  /**
   * Handle execution result and update state
   */
  private handleExecutionResult(result: CommandResult<T>): void {
    this._isExecuting.set(false);
    this._lastResult.set(result);
    this._executionCount.update(count => count + 1);

    // Add to history
    this.executionHistory.push(result);
    if (this.executionHistory.length > this.maxHistorySize) {
      this.executionHistory.shift();
    }
  }

  /**
   * Get execution history
   */
  getExecutionHistory(): CommandResult<T>[] {
    return [...this.executionHistory];
  }

  /**
   * Clear execution history
   */
  clearHistory(): void {
    this.executionHistory.length = 0;
  }

  /**
   * Get the last successful result
   */
  getLastSuccessfulResult(): CommandResult<T> | null {
    return this.executionHistory
      .slice()
      .reverse()
      .find(result => result.success) || null;
  }

  /**
   * Get the last error result
   */
  getLastErrorResult(): CommandResult<T> | null {
    return this.executionHistory
      .slice()
      .reverse()
      .find(result => !result.success) || null;
  }
}

/**
 * Command history entry interface
 */
export interface CommandHistoryEntry {
  command: ICommand;
  context?: CommandContext;
}

/**
 * Command Invoker class for managing and executing commands
 */
@Injectable({
  providedIn: 'root'
})
export class CommandInvoker {
  private readonly commandHistory: CommandHistoryEntry[] = [];
  private readonly maxHistorySize = 50;

  // Invoker state signals
  private readonly _isExecuting = signal<boolean>(false);
  private readonly _lastExecutedCommand = signal<string | null>(null);

  readonly isExecuting = this._isExecuting.asReadonly();
  readonly lastExecutedCommand = this._lastExecutedCommand.asReadonly();

  /**
   * Execute a command through the invoker
   */
  executeCommand<T>(command: ICommand<T>, context?: CommandContext): Observable<CommandResult<T>> {
    this._isExecuting.set(true);
    this._lastExecutedCommand.set(command.name);

    // Add to history
    this.commandHistory.push({ command, context });
    if (this.commandHistory.length > this.maxHistorySize) {
      this.commandHistory.shift();
    }

    return new Observable(observer => {
      command.execute(context).subscribe({
        next: (result) => {
          this._isExecuting.set(false);
          observer.next(result);
          observer.complete();
        },
        error: (error) => {
          this._isExecuting.set(false);
          observer.error(error);
        }
      });
    });
  }

  /**
   * Get command execution history
   */
  getCommandHistory(): CommandHistoryEntry[] {
    return [...this.commandHistory];
  }

  /**
   * Clear command history
   */
  clearCommandHistory(): void {
    this.commandHistory.length = 0;
  }

  /**
   * Get the last executed command
   */
  getLastCommand(): CommandHistoryEntry | null {
    return this.commandHistory[this.commandHistory.length - 1] || null;
  }
}
